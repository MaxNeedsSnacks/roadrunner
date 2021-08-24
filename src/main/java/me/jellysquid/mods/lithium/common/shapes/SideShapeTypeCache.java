package me.jellysquid.mods.lithium.common.shapes;

import com.google.common.base.Preconditions;
import me.jellysquid.mods.lithium.common.shapes.hash.VoxelShapeHash;
import me.jellysquid.mods.lithium.common.util.collections.RepeatedlyAccessedMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SideShapeType;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.EmptyBlockView;

public class SideShapeTypeCache {
    private static final ThreadLocal<ThreadState> threadLocalState = ThreadLocal.withInitial(ThreadState::new);

    public static boolean isSupporting(SideShapeType supportType, BlockState state, Direction direction) {
        ThreadState threadState = threadLocalState.get();
        VoxelShape shape = threadState.getShape(state);
        ShapeResultCache specificCache = threadState.cachedResults.computeIfAbsent(shape, $ -> new ShapeResultCache());
        return specificCache.getOrCompute(shape, direction, supportType);
    }

    private static final VoxelShape CENTER_SUPPORT_SHAPE = Block.createCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 10.0D, 9.0D);
    private static final VoxelShape RIGID_SUPPORT_SHAPE = VoxelShapes.combineAndSimplify(
            VoxelShapes.fullCube(), Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D), BooleanBiFunction.ONLY_FIRST
    );

    private static boolean computeIsSupporting(VoxelShape shape, Direction direction, SideShapeType type) {
        // VanillaCopy: SideShapeType#matches with getSidesShape replaced
        switch (type) {
            case FULL:
                return Block.isFaceFullSquare(shape, direction);
            case CENTER:
                return !VoxelShapes.matchesAnywhere(
                        shape.getFace(direction), CENTER_SUPPORT_SHAPE, BooleanBiFunction.ONLY_SECOND
                );
            case RIGID:
                return !VoxelShapes.matchesAnywhere(
                        shape.getFace(direction), RIGID_SUPPORT_SHAPE, BooleanBiFunction.ONLY_SECOND
                );
            default:
                throw new IllegalArgumentException("Unknown support type: " + type.name());
        }
    }

    private static class ThreadState {
        // Putting this into thread-local state means we don't have to worry about synchronization, and it's usually
        // "good enough" to have thread-wise caching
        private final RepeatedlyAccessedMap<VoxelShape, ShapeResultCache> cachedResults = new RepeatedlyAccessedMap<>(VoxelShapeHash.INSTANCE);
        private VoxelShape blockShape;
        private BlockState stateForShape;

        public VoxelShape getShape(BlockState state) {
            if (state != stateForShape) {
                stateForShape = state;
                blockShape = state.getSidesShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
            }
            return blockShape;
        }
    }

    private static class ShapeResultCache {
        private static final int NUM_DIRECTIONS = 6;
        private static final int NUM_TYPES = SideShapeType.values().length;

        static {
            Preconditions.checkState(NUM_DIRECTIONS == Direction.values().length);
        }

        private final Boolean[] values = new Boolean[NUM_DIRECTIONS * NUM_TYPES];

        public boolean getOrCompute(VoxelShape shape, Direction direction, SideShapeType type) {
            int index = type.ordinal() * NUM_DIRECTIONS + direction.ordinal();
            if (values[index] == null) {
                values[index] = computeIsSupporting(shape, direction, type);
            }
            return values[index];
        }
    }
}
