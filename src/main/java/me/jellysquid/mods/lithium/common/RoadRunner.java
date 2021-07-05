package me.jellysquid.mods.lithium.common;

import me.jellysquid.mods.lithium.common.config.RoadRunnerForgeConfig;
import me.jellysquid.mods.lithium.common.config.RoadRunnerRuleConfig;
import net.minecraftforge.fml.CrashReportExtender;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(RoadRunner.MODID)
public class RoadRunner {
    public static final String MODID = "roadrunner";

    public static RoadRunnerRuleConfig RULE_CONFIG;

    public static Logger LOGGER = LogManager.getLogger();

    public RoadRunner() {
        CrashReportExtender.registerCrashCallable("RoadRunner != Lithium",
                () -> "This instance was launched using RoadRunner, which is an *unofficial* Lithium fork! Please **do not** report bugs to them!");

        if (RULE_CONFIG == null) {
            throw new IllegalStateException("The mixin plugin did not initialize the config! Did it not load?");
        }

        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RoadRunnerForgeConfig.COMMON_SPEC, "roadrunner/common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, RoadRunnerForgeConfig.CLIENT_SPEC, "roadrunner/client.toml");
    }
}
