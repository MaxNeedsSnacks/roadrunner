package me.jellysquid.mods.lithium.common.ai.pathing;

import me.jellysquid.mods.lithium.api.pathing.BlockPathingBehavior;
import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraftforge.common.extensions.IForgeBlock;

public interface DetailedBlockPathingBehavior extends BlockPathingBehavior {
    /**
     * Indicates whether the cached node type is valid for this block or whether
     * {@link IForgeBlock#getAiPathNodeType(BlockState, BlockView, BlockPos, MobEntity)} needs to be checked to find
     * the correct type.
     */
    boolean needsDynamicNodeTypeCheck();

    boolean needsDynamicBurningCheck();
}
