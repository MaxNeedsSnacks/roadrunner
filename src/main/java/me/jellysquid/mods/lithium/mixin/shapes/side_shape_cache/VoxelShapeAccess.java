package me.jellysquid.mods.lithium.mixin.shapes.side_shape_cache;

import net.minecraft.util.shape.VoxelSet;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VoxelShape.class)
public interface VoxelShapeAccess {
    @Accessor
    VoxelSet getVoxels();
}
