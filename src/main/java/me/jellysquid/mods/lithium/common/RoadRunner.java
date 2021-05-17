package me.jellysquid.mods.lithium.common;

import me.jellysquid.mods.lithium.common.config.RoadRunnerConfig;
import net.minecraftforge.fml.common.Mod;

@Mod(RoadRunner.MODID)
public class RoadRunner {
    public static final String MODID = "roadrunner";

    public static RoadRunnerConfig CONFIG;

    public RoadRunner() {
        if (CONFIG == null) {
            throw new IllegalStateException("The mixin plugin did not initialize the config! Did it not load?");
        }
    }
}
