package me.jellysquid.mods.lithium.common.shapes.hash;

import it.unimi.dsi.fastutil.Hash;
import me.jellysquid.mods.lithium.mixin.shapes.side_shape_cache.SlicedShapeAccess;
import me.jellysquid.mods.lithium.mixin.shapes.side_shape_cache.VoxelShapeAccess;
import net.minecraft.util.shape.SlicedVoxelShape;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.util.shape.VoxelShape;

import java.util.Objects;

public class SlicedVoxelShapeHash implements Hash.Strategy<SlicedVoxelShape> {
    public static final SlicedVoxelShapeHash INSTANCE = new SlicedVoxelShapeHash();

    @Override
    public int hashCode(SlicedVoxelShape o) {
        SlicedShapeAccess access = access(o);
        int result = Objects.hashCode(access.getAxis());
        result = 31 * result + VoxelSetHash.INSTANCE.hashCode(getPart(o));
        result = 31 * result + VoxelShapeHash.INSTANCE.hashCode(access.getShape());
        return result;
    }

    @Override
    public boolean equals(SlicedVoxelShape a, SlicedVoxelShape b) {
        if (a == b) {
            return true;
        } else if (a == null || b == null) {
            return false;
        }
        SlicedShapeAccess accessA = access(a);
        SlicedShapeAccess accessB = access(b);
        return Objects.equals(accessA.getAxis(), accessB.getAxis()) &&
                VoxelShapeHash.INSTANCE.equals(accessA.getShape(), accessB.getShape()) &&
                VoxelSetHash.INSTANCE.equals(getPart(a), getPart(b));
    }

    private static SlicedShapeAccess access(SlicedVoxelShape a) {
        return (SlicedShapeAccess) a;
    }

    private static VoxelSet getPart(VoxelShape a) {
        return ((VoxelShapeAccess) a).getVoxels();
    }
}
