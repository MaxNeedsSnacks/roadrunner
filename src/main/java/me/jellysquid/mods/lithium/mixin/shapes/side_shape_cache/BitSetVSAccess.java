package me.jellysquid.mods.lithium.mixin.shapes.side_shape_cache;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.BitSet;

import net.minecraft.util.shape.BitSetVoxelSet;

@Mixin(BitSetVoxelSet.class)
public interface BitSetVSAccess {
    @Accessor
    BitSet getStorage();

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
