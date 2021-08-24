package me.jellysquid.mods.lithium.mixin.shapes.side_shape_cache;

import com.google.common.base.Preconditions;
import me.jellysquid.mods.lithium.common.shapes.SideShapeTypeCache;
import net.minecraft.block.BlockState;
import net.minecraft.block.SideShapeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.EmptyBlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.block.AbstractBlock$AbstractBlockState$ShapeCache")
public class BlockCacheMixin {
    @Redirect(
            method = "<init>(Lnet/minecraft/block/BlockState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/SideShapeType;matches(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z")
    )
    private boolean redirectIsSupporting(
            SideShapeType supportType, BlockState state, BlockView level, BlockPos blockPos, Direction direction
    ) {
        Preconditions.checkState(level == EmptyBlockView.INSTANCE);
        Preconditions.checkState(blockPos == BlockPos.ORIGIN);
        return SideShapeTypeCache.isSupporting(supportType, state, direction);
    }
}
