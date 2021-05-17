package me.jellysquid.mods.lithium.common.entity.tracker.nearby;

import me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerEngine;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.ChunkSectionPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows for multiple listeners on an entity to be grouped under one logical listener. No guarantees are made about the
 * order of which each sub-listener will be notified.
 */
public class NearbyEntityListenerMulti implements NearbyEntityListener {
    private final List<NearbyEntityListener> listeners = new ArrayList<>();
    @Nullable
    private EntityTrackerEngine trackingEngine;

    public void addListener(NearbyEntityListener listener) {
        ChunkSectionPos listenerLocation = null;
        if (trackingEngine != null) {
            // This should be a rare case: A listener goal is added after the entity is added to the world (e.g.
            // in EntityJoinWorldEvent). In this case we remove the listener before modifying it and readd it again
            // afterwards, to make sure that the data structures stay up to date
            listenerLocation = trackingEngine.removeListener(this);
        }
        this.listeners.add(listener);
        if (listenerLocation != null) {
            trackingEngine.addListener(
                    listenerLocation.getSectionX(), listenerLocation.getSectionY(), listenerLocation.getSectionZ(), this
            );
        }
    }

    public void setTrackingEngine(@Nonnull EntityTrackerEngine trackingEngine) {
        if (this.trackingEngine != null && this.trackingEngine != trackingEngine) {
            throw new IllegalStateException("Adding to engine " + trackingEngine + " but is still in " + this.trackingEngine);
        }
        this.trackingEngine = trackingEngine;
    }

    public void clearTrackingEngine() {
        this.trackingEngine = null;
    }

    @Override
    public int getChunkRange() {
        int range = 0;

        for (NearbyEntityListener listener : this.listeners) {
            range = Math.max(range, listener.getChunkRange());
        }

        return range;
    }

    @Override
    public void onEntityEnteredRange(LivingEntity entity) {
        for (NearbyEntityListener listener : this.listeners) {
            listener.onEntityEnteredRange(entity);
        }
    }

    @Override
    public void onEntityLeftRange(LivingEntity entity) {
        for (NearbyEntityListener listener : this.listeners) {
            listener.onEntityLeftRange(entity);
        }
    }

    @Override
    public String toString() {
        StringBuilder sublisteners = new StringBuilder();
        String comma = "";
        for (NearbyEntityListener listener : this.listeners) {
            sublisteners.append(comma).append(listener.toString());
            comma = ","; //trick to drop the first comma
        }

        return super.toString() + " with sublisteners: [" + sublisteners + "]";
    }
}
