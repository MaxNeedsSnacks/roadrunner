package me.jellysquid.mods.lithium.common.ai.pathing;

import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.fluid.WaterFluid;

// FIXME: forge bakes blockstates when they're freezing data, so this entire file is :concern:
public class PathNodeDefaults {
    public static PathNodeType getNeighborNodeType(BlockState state) {
        if (state.isAir()) {
            return PathNodeType.OPEN;
        }

        // [VanillaCopy] LandPathNodeMaker#getNodeTypeFromNeighbors
        // Determine what kind of obstacle type this neighbor is
        if (state.isOf(Blocks.CACTUS)) {
            return PathNodeType.DANGER_CACTUS;
        } else if (state.isOf(Blocks.SWEET_BERRY_BUSH)) {
            return PathNodeType.DANGER_OTHER;
        } else if (isFireDangerSource(state)) {
            return PathNodeType.DANGER_FIRE;
        } else if (state.getFluidState().getFluid() instanceof WaterFluid) {
            // can't use tags here thanks to data freezing
            return PathNodeType.WATER_BORDER;
        } else {
            return PathNodeType.OPEN;
        }
    }

    public static PathNodeType getNodeType(BlockState state) {
        if (state.isAir()) {
            return PathNodeType.OPEN;
        }

        Block block = state.getBlock();
        Material material = state.getMaterial();

        // same thing as above, thanks forge
        if (block instanceof TrapdoorBlock || state.isOf(Blocks.LILY_PAD)) {
            return PathNodeType.TRAPDOOR;
        }

        if (state.isOf(Blocks.CACTUS)) {
            return PathNodeType.DAMAGE_CACTUS;
        }

        if (state.isOf(Blocks.SWEET_BERRY_BUSH)) {
            return PathNodeType.DAMAGE_OTHER;
        }

        if (state.isOf(Blocks.HONEY_BLOCK)) {
            return PathNodeType.STICKY_HONEY;
        }

        if (state.isOf(Blocks.COCOA)) {
            return PathNodeType.COCOA;
        }

        if (isFireDangerSource(state)) {
            return PathNodeType.DAMAGE_FIRE;
        }

        if (DoorBlock.isWoodenDoor(state) && !state.get(DoorBlock.OPEN)) {
            return PathNodeType.DOOR_WOOD_CLOSED;
        }

        if ((block instanceof DoorBlock) && (material == Material.METAL) && !state.get(DoorBlock.OPEN)) {
            return PathNodeType.DOOR_IRON_CLOSED;
        }

        if ((block instanceof DoorBlock) && state.get(DoorBlock.OPEN)) {
            return PathNodeType.DOOR_OPEN;
        }

        if (block instanceof AbstractRailBlock) {
            return PathNodeType.RAIL;
        }

        if (block instanceof LeavesBlock) {
            return PathNodeType.LEAVES;
        }

        if (block instanceof FenceBlock || block instanceof WallBlock || ((block instanceof FenceGateBlock) && !state.get(FenceGateBlock.OPEN))) {
            return PathNodeType.FENCE;
        }

        // Retrieve the fluid state from the block state to avoid a second lookup
        FluidState fluid = state.getFluidState();

        if (fluid.getFluid() instanceof WaterFluid) {
            return PathNodeType.WATER;
        } else if (fluid.getFluid() instanceof LavaFluid) {
            return PathNodeType.LAVA;
        }

        return PathNodeType.OPEN;
    }

    private static boolean isFireDangerSource(BlockState blockState) {
        Block block = blockState.getBlock();
        return block instanceof AbstractFireBlock || block.is(Blocks.LAVA) || block.is(Blocks.MAGMA_BLOCK) || (block instanceof CampfireBlock && blockState.get(CampfireBlock.LIT));
    }
}
