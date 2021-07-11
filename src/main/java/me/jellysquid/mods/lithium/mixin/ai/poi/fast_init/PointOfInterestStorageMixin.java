package me.jellysquid.mods.lithium.mixin.ai.poi.fast_init;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import me.jellysquid.mods.lithium.common.world.interests.PointOfInterestTypeHelper;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.poi.PointOfInterestSet;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.storage.SerializingRegionBasedStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Mixin(PointOfInterestStorage.class)
public abstract class PointOfInterestStorageMixin extends SerializingRegionBasedStorage<PointOfInterestSet> implements PointOfInterestTypeHelper.EnabledMarker {
    public PointOfInterestStorageMixin(File directory, Function<Runnable, Codec<PointOfInterestSet>> function, Function<Runnable, PointOfInterestSet> function2, DataFixer dataFixer, DataFixTypes dataFixTypes, boolean bl) {
        super(directory, function, function2, dataFixer, dataFixTypes, bl);
    }

    @Shadow
    protected abstract void scanAndPopulate(ChunkSection section, ChunkSectionPos sectionPos, BiConsumer<BlockPos, PointOfInterestType> entryConsumer);

    /**
     * @reason Avoid Stream API
     * @author Jellysquid
     */
    @Overwrite
    public void initForPalette(ChunkPos chunkPos_1, ChunkSection section) {
        ChunkSectionPos sectionPos = ChunkSectionPos.from(chunkPos_1, section.getYOffset() >> 4);

        PointOfInterestSet set = this.get(sectionPos.asLong()).orElse(null);

        if (set != null) {
            set.updatePointsOfInterest((consumer) -> {
                if (PointOfInterestTypeHelper.shouldScan(section)) {
                    this.scanAndPopulate(section, sectionPos, consumer);
                }
            });
        } else {
            if (PointOfInterestTypeHelper.shouldScan(section)) {
                set = this.getOrCreate(sectionPos.asLong());

                this.scanAndPopulate(section, sectionPos, set::add);
            }
        }
    }
}
