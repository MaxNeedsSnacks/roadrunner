package me.jellysquid.mods.lithium.mixin.shapes.side_shape_cache;

import net.minecraft.util.shape.CroppedVoxelSet;
import net.minecraft.util.shape.VoxelSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CroppedVoxelSet.class)
public interface CroppedVoxelSetAccess {
    @Accessor
    VoxelSet getParent();

    @Accessor
    int getXMin();

    @Accessor
    int getYMin();

    @Accessor
    int getZMin();

    @Accessor
    int getXMax();

    @Accessor
    int getYMax();

    @Accessor
    int getZMax();
}
