package me.jellysquid.mods.lithium.common;

import me.jellysquid.mods.lithium.common.config.RoadRunnerConfig;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(RoadRunner.MODID)
public class RoadRunner {
    public static final String MODID = "roadrunner";

    public static RoadRunnerConfig CONFIG;

    public static Logger LOGGER = LogManager.getLogger();

    public RoadRunner() {
        if (CONFIG == null) {
            throw new IllegalStateException("The mixin plugin did not initialize the config! Did it not load?");
        }
    }
}
