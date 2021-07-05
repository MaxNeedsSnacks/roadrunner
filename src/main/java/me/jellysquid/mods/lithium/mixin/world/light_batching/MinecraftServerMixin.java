package me.jellysquid.mods.lithium.mixin.world.light_batching;

import me.jellysquid.mods.lithium.common.config.RoadRunnerForgeConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow
    public abstract Iterable<ServerWorld> getWorlds();

    @Inject(
            method = "prepareStartRegion",
            at = @At("RETURN")
    )
    private void setLightBatchSize(WorldGenerationProgressListener l, CallbackInfo ci) {
        int batchSize = RoadRunnerForgeConfig.COMMON.lightBatchSize.get();
        for (ServerWorld world : getWorlds()) {
            world.getChunkManager().getLightingProvider().setTaskBatchSize(batchSize);
        }
    }
}
