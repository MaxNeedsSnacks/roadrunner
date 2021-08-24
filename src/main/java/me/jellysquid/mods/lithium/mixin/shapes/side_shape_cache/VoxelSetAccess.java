package me.jellysquid.mods.lithium.mixin.shapes.side_shape_cache;

import net.minecraft.util.shape.VoxelSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VoxelSet.class)
public interface VoxelSetAccess {
    @Accessor
    int getXSize();

    @Accessor
    int getYSize();

    @Accessor
    int getZSize();
}
