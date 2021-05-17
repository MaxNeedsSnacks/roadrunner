package me.jellysquid.mods.lithium.mixin.gen.cached_generator_settings;

import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Supplier;

@Mixin(NoiseChunkGenerator.class)
public class NoiseChunkGeneratorMixin {

    // ported from jamierocks' lithium-forge: mixin cannot hook into constructors other than head and tail

    @Shadow
    @Final
    protected Supplier<ChunkGeneratorSettings> settings;

    private int cachedSeaLevel = -1;

    /**
     * Use cached sea level instead of retrieving from the registry every time.
     * This method is called for every block in the chunk so this will save a lot of registry lookups.
     *
     * @author SuperCoder79
     * @reason Using a cached value here.
     */
    @Overwrite
    public int getSeaLevel() {
        if (cachedSeaLevel == -1) {
            this.cachedSeaLevel = this.settings.get().getSeaLevel();
        }
        return this.cachedSeaLevel;
    }

    /**
     * Initialize the cache early in the ctor to avoid potential future problems with uninitialized usages
     */
    /*@Inject(
            method = "<init>(Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;JLjava/util/function/Supplier;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/gen/chunk/ChunkGeneratorSettings;getGenerationShapeConfig()Lnet/minecraft/world/gen/chunk/GenerationShapeConfig;",
                    shift = At.Shift.BEFORE
            )
    )
    private void hookConstructor(BiomeSource populationSource, BiomeSource biomeSource, long seed, Supplier<ChunkGeneratorSettings> settings, CallbackInfo ci) {
        this.cachedSeaLevel = settings.get().getSeaLevel();
    }*/
}
