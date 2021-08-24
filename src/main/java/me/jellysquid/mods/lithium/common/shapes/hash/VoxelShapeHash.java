package me.jellysquid.mods.lithium.common.shapes.hash;

import it.unimi.dsi.fastutil.Hash;
import me.jellysquid.mods.lithium.mixin.shapes.side_shape_cache.VoxelShapeAccess;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.SimpleVoxelShape;
import net.minecraft.util.shape.SlicedVoxelShape;
import net.minecraft.util.shape.VoxelShape;

public class VoxelShapeHash implements Hash.Strategy<VoxelShape> {
    public static final VoxelShapeHash INSTANCE = new VoxelShapeHash();

    @Override
    public int hashCode(VoxelShape o) {
        if (o instanceof SlicedVoxelShape) {
            return SlicedVoxelShapeHash.INSTANCE.hashCode((SlicedVoxelShape) o);
        } else if (o instanceof ArrayVoxelShape) {
            return ArrayVoxelShapeHash.INSTANCE.hashCode((ArrayVoxelShape) o);
        } else if (o instanceof SimpleVoxelShape) {
            return VoxelSetHash.INSTANCE.hashCode(((VoxelShapeAccess) o).getVoxels());
        } else {
            return o.hashCode();
        }
    }

    @Override
    public boolean equals(VoxelShape a, VoxelShape b) {
        if (a == b) {
            return true;
        } else if (a == null || b == null) {
            return false;
        } else if (a.getClass() != b.getClass()) {
            return false;
        } else if (a instanceof SlicedVoxelShape) {
            return SlicedVoxelShapeHash.INSTANCE.equals((SlicedVoxelShape) a, (SlicedVoxelShape) b);
        } else if (a instanceof ArrayVoxelShape) {
            return ArrayVoxelShapeHash.INSTANCE.equals((ArrayVoxelShape) a, (ArrayVoxelShape) b);
        } else if (a instanceof SimpleVoxelShape) {
            return VoxelSetHash.INSTANCE.equals(
                    ((VoxelShapeAccess) a).getVoxels(), ((VoxelShapeAccess) b).getVoxels()
            );
        } else {
            return a.equals(b);
        }
    }
}
