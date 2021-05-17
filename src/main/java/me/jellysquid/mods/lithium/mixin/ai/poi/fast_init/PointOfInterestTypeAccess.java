package me.jellysquid.mods.lithium.mixin.ai.poi.fast_init;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import me.jellysquid.mods.lithium.common.world.interests.PointOfInterestTypeHelper;
import net.minecraft.block.BlockState;
import net.minecraft.world.poi.PointOfInterestType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(PointOfInterestType.class)
public interface PointOfInterestTypeAccess {
    @Accessor("BLOCK_STATE_TO_POINT_OF_INTEREST_TYPE")
    static Map<BlockState, PointOfInterestType> getBlockStateToPointOfInterestType() {
        throw new UnsupportedOperationException("Replaced by Mixin");
    }

    @Accessor("BLOCK_STATE_TO_POINT_OF_INTEREST_TYPE")
    static void setBlockStateToPointOfInterestType(Map<BlockState, PointOfInterestType> newMap) {
        throw new UnsupportedOperationException("Replaced by Mixin");
    }
}
