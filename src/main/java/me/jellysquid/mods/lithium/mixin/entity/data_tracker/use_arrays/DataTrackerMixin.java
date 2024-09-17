package me.jellysquid.mods.lithium.mixin.entity.data_tracker.use_arrays;

import me.jellysquid.mods.lithium.common.util.collections.CopyOnWriteI2OOpenHashMap;
import me.jellysquid.mods.lithium.common.util.lock.NullReadBasicWriteLock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Optimizes the DataTracker to use a simple array-based storage for entries and avoids integer boxing. This reduces
 * a lot of the overhead associated with retrieving tracked data about an entity.
 */
@Mixin(DataTracker.class)
public abstract class DataTrackerMixin {
    @Mutable
    @Shadow
    @Final
    private Map<Integer, DataTracker.Entry<?>> entries;

    @Shadow
    @Final
    @Mutable
    private ReadWriteLock lock;

    /**
     * Mirrors the vanilla backing entries map. Each DataTracker.Entry can be accessed in this array through its ID.
     * Copy-on-write.
     **/
    private volatile DataTracker.Entry<?>[] entriesArray = new DataTracker.Entry<?>[0];

    /**
     * Re-initialize the backing entries map with an optimized variant to speed up iteration in packet code and to
     * save memory.
     */
    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void reinit(Entity trackedEntity, CallbackInfo ci) {
        this.entries = new CopyOnWriteI2OOpenHashMap<>(this.entries);
        // Read lock is a null lock (CoW removes need for synchronization there)
        // Write lock is a "basic" lock to synchronize CoW updates
        this.lock = new NullReadBasicWriteLock();
    }

    /**
     * We redirect the call to add a tracked data to the internal map so we can add it to our new storage structure. This
     * should only ever occur during entity initialization. Type-erasure is a bit of a pain here since we must redirect
     * a calls to the generic Map interface.
     */
    @Redirect(
            method = "addTrackedData",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
            )
    )
    private Object onAddTrackedDataInsertMap(Map<Class<? extends Entity>, Integer> map, /* Integer */ Object keyRaw, /* DataTracker.Entry<?> */ Object valueRaw) {
        int k = (int) keyRaw;
        DataTracker.Entry<?> v = (DataTracker.Entry<?>) valueRaw;

        // Create sufficiently large copy of array
        DataTracker.Entry<?>[] storage = Arrays.copyOf(this.entriesArray, Math.max(k + 1, this.entriesArray.length));

        // Update the new entry
        storage[k] = v;
        // publish results, first the vanilla map (now CoW, so put implicitly publishes), then our array.
        // Order is important here, all methods can fall back to the vanilla map, but not all can use the array.
        Object result = this.entries.put(k, v);
        this.entriesArray = storage;

        return result;
    }

    /**
     * @reason Avoid integer boxing/unboxing and use our array-based storage
     * @author JellySquid
     */
    @Overwrite
    public <T> DataTracker.Entry<T> getEntry(TrackedData<T> data) {
        try {
            DataTracker.Entry<?>[] array = this.entriesArray;

            int id = data.getId();

            // The vanilla implementation will simply return null if the tracker doesn't contain the specified entry. However,
            // accessing an array with an invalid pointer will throw a OOB exception, where-as a HashMap would simply
            // return null. We check this case (which should be free, even if so insignificant, as the subsequent bounds
            // check will hopefully be eliminated)
            if (id < 0 || id >= array.length || array[id] == null) {
                // Fall back to vanilla map if the array did not have the entry. This should only ever happen when
                // racing between adding an entry and getting it.
                //noinspection unchecked,SuspiciousMethodCalls
                return (DataTracker.Entry<T>) this.entries.get(id);
            }

            // This cast can fail if trying to access a entry which doesn't belong to this tracker, as the ID could
            // instead point to an entry of a different type. However, that is also vanilla behaviour.
            // noinspection unchecked
            return (DataTracker.Entry<T>) array[id];
        } catch (Throwable cause) {
            // Move to another method so this function can be in-lined better
            throw onGetException(cause, data);
        }
    }

    private static <T> CrashException onGetException(Throwable cause, TrackedData<T> data) {
        CrashReport report = CrashReport.create(cause, "Getting synced entity data");

        CrashReportSection section = report.addElement("Synced entity data");
        section.add("Data ID", data);

        return new CrashException(report);
    }
}
