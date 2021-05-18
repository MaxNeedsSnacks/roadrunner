package me.jellysquid.mods.lithium.mixin.entity.skip_fire_check;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    private int fireTicks;

    @Shadow
    protected abstract int getBurningDuration();

    @Redirect(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/BlockPos;stream(Lnet/minecraft/util/math/Box;)Ljava/util/stream/Stream;"
            )
    )
    private Stream<BlockPos> skipFireTestIfResultDoesNotMatter(Box arg) {
        // Skip scanning the blocks around the entity touches by returning an empty stream when the result does not matter
        if (this.fireTicks > 0 || this.fireTicks == -this.getBurningDuration()) {
            return Stream.empty();
        }

        return BlockPos.stream(arg);
    }
}
