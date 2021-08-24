package me.jellysquid.mods.lithium.common.shapes.hash;

import it.unimi.dsi.fastutil.Hash;
import me.jellysquid.mods.lithium.mixin.shapes.side_shape_cache.ArrayVSAccess;
import me.jellysquid.mods.lithium.mixin.shapes.side_shape_cache.VoxelShapeAccess;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.util.shape.VoxelShape;

import java.util.Objects;

public class ArrayVoxelShapeHash implements Hash.Strategy<ArrayVoxelShape> {
    public static final ArrayVoxelShapeHash INSTANCE = new ArrayVoxelShapeHash();

    @Override
    public int hashCode(ArrayVoxelShape o) {
        ArrayVSAccess access = access(o);
        return 31 * Objects.hash(access.getXPoints(), access.getYPoints(), access.getZPoints())
                + VoxelSetHash.INSTANCE.hashCode(getPart(o));
    }

    @Override
    public boolean equals(ArrayVoxelShape a, ArrayVoxelShape b) {
        if (a == b) {
            return true;
        } else if (a == null || b == null) {
            return false;
        }
        ArrayVSAccess accessA = access(a);
        ArrayVSAccess accessB = access(b);
        return Objects.equals(accessA.getXPoints(), accessB.getXPoints()) &&
                Objects.equals(accessA.getYPoints(), accessB.getYPoints()) &&
                Objects.equals(accessA.getZPoints(), accessB.getZPoints()) &&
                VoxelSetHash.INSTANCE.equals(getPart(a), getPart(b));
    }

    @SuppressWarnings("ConstantConditions")
    private static ArrayVSAccess access(ArrayVoxelShape a) {
        return (ArrayVSAccess) (Object) a;
    }

    private static VoxelSet getPart(VoxelShape a) {
        return ((VoxelShapeAccess) a).getVoxels();
    }
}
