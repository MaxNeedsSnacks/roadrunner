package me.jellysquid.mods.lithium.mixin.shapes.side_shape_cache;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.shape.ArrayVoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ArrayVoxelShape.class)
public interface ArrayVSAccess {
    @Accessor
    DoubleList getXPoints();

    @Accessor
    DoubleList getYPoints();

    @Accessor
    DoubleList getZPoints();
}
