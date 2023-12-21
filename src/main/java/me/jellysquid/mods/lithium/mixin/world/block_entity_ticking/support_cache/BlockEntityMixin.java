package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.support_cache;

import me.jellysquid.mods.lithium.common.world.blockentity.SupportCache;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements SupportCache {
    @Shadow
    public abstract BlockState getCachedState();

    @Shadow
    public abstract BlockEntityType<?> getType();

    @Shadow
    @Nullable
    private BlockState cachedState;

    private BlockState supportTestState;
    private boolean supportTestResult;

    @Override
    public boolean isSupported() {
        BlockState state = this.cachedState;
        if(state != null && this.supportTestState == state) {
            return this.supportTestResult;
        }
        return computeIsSupported();

    }

    private boolean computeIsSupported() {
        return this.supportTestResult = this.getType().supports((this.supportTestState = this.getCachedState()).getBlock());
    }
}
