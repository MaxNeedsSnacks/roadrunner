package me.jellysquid.mods.lithium.mixin.entity.fast_fluid_check;

import com.google.common.collect.Iterables;
import me.jellysquid.mods.lithium.common.fluid.RequiredTagsByName;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * In most cases the fluid at the head of an entity is not contained in any fluid tag (empty fluid). Even if this is not
 * the case there is usually a single tag containing the fluid. In these two cases it is much faster to get the tag at
 * the head of the entity by looking at the reverse tags for the fluid, rather than testing for each tag individually.
 * This accounts for 1-2% of server tick time without this mixin in the Enigmatic 6 data (July 10, 2021).
 */
@Mixin(Entity.class)
public class EntityMixin {
    @Shadow
    @Nullable
    protected Tag<Fluid> field_25599;

    @Shadow
    public World world;

    @Inject(
            method = "updateSubmergedInWaterState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/tag/FluidTags;getRequiredTags()Ljava/util/List;"
            ),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void fastFluidCheck(CallbackInfo ci, double checkY, Entity vehicle, BlockPos headPos, FluidState fluidAt) {
        Set<Identifier> containingTags = fluidAt.getFluid().getTags();
        if (containingTags.isEmpty()) {
            // No tag => leave field_25599 as null
            ci.cancel();
        } else if (containingTags.size() == 1) {
            // Only contained in one tag => set to this one
            double d1 = (float) headPos.getY() + fluidAt.getHeight(this.world, headPos);
            if (d1 > checkY) {
                Identifier tagName = containingTags.iterator().next();
                this.field_25599 = RequiredTagsByName.getRequiredTagsByName().get(tagName);
            }
            ci.cancel();
        }
        // Otherwise: Chosen tag depends on iteration order, which we do not control => use vanilla logic
    }
}
