package me.jellysquid.mods.lithium.common.shapes.hash;


import it.unimi.dsi.fastutil.Hash;
import me.jellysquid.mods.lithium.mixin.shapes.side_shape_cache.BitSetVSAccess;
import me.jellysquid.mods.lithium.mixin.shapes.side_shape_cache.VoxelSetAccess;
import me.jellysquid.mods.lithium.mixin.shapes.side_shape_cache.CroppedVoxelSetAccess;
import net.minecraft.util.shape.BitSetVoxelSet;
import net.minecraft.util.shape.CroppedVoxelSet;
import net.minecraft.util.shape.VoxelSet;

import java.util.Objects;

public class VoxelSetHash implements Hash.Strategy<VoxelSet> {
    public static final VoxelSetHash INSTANCE = new VoxelSetHash();

    @Override
    public int hashCode(VoxelSet o) {
        VoxelSetAccess generalAccess = (VoxelSetAccess) o;
        int result = generalAccess.getXSize();
        result = 31 * result + generalAccess.getYSize();
        result = 31 * result + generalAccess.getZSize();
        if (o instanceof CroppedVoxelSet) {
            CroppedVoxelSetAccess access = access((CroppedVoxelSet) o);
            result = 31 * result + access.getXMin();
            result = 31 * result + access.getYMin();
            result = 31 * result + access.getZMin();
            result = 31 * result + access.getXMax();
            result = 31 * result + access.getYMax();
            result = 31 * result + access.getZMax();
            result = 31 * result + hashCode(access.getParent());
            return result;
        } else if (o instanceof BitSetVoxelSet) {
            BitSetVSAccess access = access((BitSetVoxelSet) o);
            result = 31 * result + access.getXMin();
            result = 31 * result + access.getYMin();
            result = 31 * result + access.getZMin();
            result = 31 * result + access.getXMax();
            result = 31 * result + access.getYMax();
            result = 31 * result + access.getZMax();
            result = 31 * result + Objects.hashCode(access.getStorage());
            return result;
        } else {
            return 31 * result + Objects.hashCode(o);
        }
    }

    @Override
    public boolean equals(VoxelSet a, VoxelSet b) {
        if (a == b) {
            return true;
        } else if (a == null || b == null) {
            return false;
        } else if (a.getClass() != b.getClass()) {
            return false;
        }
        VoxelSetAccess genAccessA = (VoxelSetAccess) a;
        VoxelSetAccess genAccessB = (VoxelSetAccess) b;
        if (genAccessA.getXSize() != genAccessB.getXSize() ||
                genAccessA.getYSize() != genAccessB.getYSize() ||
                genAccessA.getZSize() != genAccessB.getZSize()
        ) {
            return false;
        }
        if (a instanceof CroppedVoxelSet) {
            CroppedVoxelSetAccess accessA = access((CroppedVoxelSet) a);
            CroppedVoxelSetAccess accessB = access((CroppedVoxelSet) b);
            return accessA.getXMax() == accessB.getXMax() &&
                    accessA.getYMax() == accessB.getYMax() &&
                    accessA.getZMax() == accessB.getZMax() &&
                    accessA.getXMin() == accessB.getXMin() &&
                    accessA.getYMin() == accessB.getYMin() &&
                    accessA.getZMin() == accessB.getZMin() &&
                    equals(accessA.getParent(), accessB.getParent());
        } else if (a instanceof BitSetVoxelSet) {
            BitSetVSAccess accessA = access((BitSetVoxelSet) a);
            BitSetVSAccess accessB = access((BitSetVoxelSet) b);
            return accessA.getXMax() == accessB.getXMax() &&
                    accessA.getYMax() == accessB.getYMax() &&
                    accessA.getZMax() == accessB.getZMax() &&
                    accessA.getXMin() == accessB.getXMin() &&
                    accessA.getYMin() == accessB.getYMin() &&
                    accessA.getZMin() == accessB.getZMin() &&
                    accessA.getStorage().equals(accessB.getStorage());
        } else {
            return a.equals(b);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static CroppedVoxelSetAccess access(CroppedVoxelSet part) {
        return (CroppedVoxelSetAccess) (Object) part;
    }

    @SuppressWarnings("ConstantConditions")
    private static BitSetVSAccess access(BitSetVoxelSet part) {
        return (BitSetVSAccess) (Object) part;
    }
}
