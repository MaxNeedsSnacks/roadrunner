package me.jellysquid.mods.lithium.mixin.ai.pathing;

import me.jellysquid.mods.lithium.api.pathing.BlockPathingBehavior;
import me.jellysquid.mods.lithium.common.ai.pathing.BlockClassChecker;
import me.jellysquid.mods.lithium.common.ai.pathing.PathNodeDefaults;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.PathNodeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class MixinBlock implements BlockPathingBehavior {

    private boolean needsDynamicNodeTypeCheck = true;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void postConstruct(AbstractBlock.Settings p_i241196_1_, CallbackInfo ci) {
        this.needsDynamicNodeTypeCheck = BlockClassChecker.shouldUseDynamicTypeCheck(this.getClass());
    }

    @Override
    public PathNodeType getPathNodeType(BlockState state) {
        return PathNodeDefaults.getNodeType(state);
    }

    @Override
    public PathNodeType getPathNodeTypeAsNeighbor(BlockState state) {
        return PathNodeDefaults.getNeighborNodeType(state);
    }

    @Override
    public boolean needsDynamicNodeTypeCheck() {
        return needsDynamicNodeTypeCheck;
    }
}
