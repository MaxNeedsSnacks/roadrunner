package me.jellysquid.mods.lithium.mixin.shapes.side_shape_cache;

import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.SlicedVoxelShape;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SlicedVoxelShape.class)
public interface SlicedShapeAccess {
    @Accessor
    VoxelShape getShape();

    @Accessor
    Direction.Axis getAxis();
}
