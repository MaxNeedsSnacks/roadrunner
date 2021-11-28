package me.jellysquid.mods.lithium.mixin.ai.poi.fast_retrieval;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.jellysquid.mods.lithium.common.util.Collector;
import me.jellysquid.mods.lithium.common.util.collections.ListeningLong2ObjectOpenHashMap;
import me.jellysquid.mods.lithium.common.world.interests.RegionBasedStorageColumn;
import me.jellysquid.mods.lithium.common.world.interests.RegionBasedStorageSectionAccess;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.storage.SerializingRegionBasedStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.BitSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType") // We don't get a choice, this is Minecraft's doing!
@Mixin(SerializingRegionBasedStorage.class)
public abstract class SerializingRegionBasedStorageMixin<R> implements RegionBasedStorageSectionAccess<R> {
    @Mutable
    @Shadow
    @Final
    private Long2ObjectMap<Optional<R>> loadedElements;

    @Shadow
    protected abstract Optional<R> get(long pos);

    @Shadow
    protected abstract void loadDataAt(ChunkPos pos);

    private Long2ObjectOpenHashMap<RegionBasedStorageColumn> columns;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(File directory, Function<Runnable, Codec<R>> function, Function<Runnable, R> function2, DataFixer dataFixer, DataFixTypes dataFixTypes, boolean sync, CallbackInfo ci) {
        this.columns = new Long2ObjectOpenHashMap<>();
        this.loadedElements = new ListeningLong2ObjectOpenHashMap<>(this::onEntryAdded, this::onEntryRemoved);
    }

    private void onEntryRemoved(long key, Optional<R> value) {
        int y = ChunkSectionPos.unpackY(key);

        if (!isSectionValid(y)) {
            return;
        }

        long pos = getChunkFromSection(key);
        RegionBasedStorageColumn flags = this.columns.get(pos);

        if (flags != null && flags.clear(y)) {
            this.columns.remove(pos);
        }
    }

    private void onEntryAdded(long key, Optional<R> value) {
        int y = ChunkSectionPos.unpackY(key);

        // We only care about items belonging to a valid sub-chunk
        if (!isSectionValid(y)) {
            return;
        }

        long pos = getChunkFromSection(key);

        RegionBasedStorageColumn flags = this.columns.get(pos);

        if (flags == null) {
            this.columns.put(pos, flags = new RegionBasedStorageColumn());
        }

        flags.set(y, value.isPresent());
    }

    @Override
    public Stream<R> getWithinChunkColumn(int chunkX, int chunkZ) {
        RegionBasedStorageColumn flags = this.getCachedColumnInfo(chunkX, chunkZ);

        // No items are present in this column
        if (flags.noSectionsPresent()) {
            return Stream.empty();
        }

        return flags.nonEmptySections()
                .mapToObj((chunkY) -> this.loadedElements.get(ChunkSectionPos.asLong(chunkX, chunkY, chunkZ)).orElse(null))
                .filter(Objects::nonNull);
    }

    @Override
    public boolean collectWithinChunkColumn(int chunkX, int chunkZ, Collector<R> consumer) {
        RegionBasedStorageColumn flags = this.getCachedColumnInfo(chunkX, chunkZ);

        // No items are present in this column
        if (flags.noSectionsPresent()) {
            return true;
        }

        for (int chunkY = flags.nextNonEmptySection(0); chunkY >= 0; chunkY = flags.nextNonEmptySection(chunkY + 1)) {
            R obj = this.loadedElements.get(ChunkSectionPos.asLong(chunkX, chunkY, chunkZ)).orElse(null);

            if (obj != null && !consumer.collect(obj)) {
                return false;
            }
        }

        return true;
    }

    private RegionBasedStorageColumn getCachedColumnInfo(int chunkX, int chunkZ) {
        long pos = ChunkPos.toLong(chunkX, chunkZ);

        RegionBasedStorageColumn flags = this.getColumnInfo(pos, false);

        if (flags != null) {
            return flags;
        }

        this.loadDataAt(new ChunkPos(pos));

        return this.getColumnInfo(pos, true);
    }

    private RegionBasedStorageColumn getColumnInfo(long pos, boolean required) {
        RegionBasedStorageColumn set = this.columns.get(pos);

        if (set == null && required) {
            throw new NullPointerException("No data is present for column: " + new ChunkPos(pos));
        }

        return set;
    }

    private static long getChunkFromSection(long section) {
        int x = ChunkSectionPos.unpackX(section);
        int z = ChunkSectionPos.unpackZ(section);
        return ChunkPos.toLong(x, z);
    }

    private static boolean isSectionValid(int y) {
        return y >= 0 && y < RegionBasedStorageColumn.SECTIONS_IN_CHUNK;
    }
}
