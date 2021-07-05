package me.jellysquid.mods.lithium.common.config;

import me.jellysquid.mods.lithium.common.RoadRunner;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.apache.commons.lang3.tuple.Pair;

@EventBusSubscriber(modid = RoadRunner.MODID, bus = EventBusSubscriber.Bus.MOD)
public class RoadRunnerForgeConfig {

    public static final class Common {
        // world
        public final ForgeConfigSpec.IntValue lightBatchSize;

        public Common(ForgeConfigSpec.Builder builder) {

            builder
                    .comment(
                            "Configuration for world-related tweaks.",
                            "Please note that these options will only work if their corresponding mixin rule is enabled as well!"
                    )
                    .push("world");
            this.lightBatchSize = builder
                    .comment(
                            "Changes the max amount of tasks that the lighting engine may process at a time.",
                            "By default, Vanilla uses a value of 500 during and 5 after initial world load,",
                            "you may however change this to any arbitrary amount as in some cases,",
                            "larger batch sizes may actually lead to *less* stalling when it comes to chunk loading."
                            )
                    .defineInRange("light_batching.size", 5, 0, Integer.MAX_VALUE);
            builder.pop();

        }
    }

    public static final class Client {
        public Client(ForgeConfigSpec.Builder builder) {

        }
    }

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        Pair<Common, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = commonSpecPair.getRight();
        COMMON = commonSpecPair.getLeft();

        Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = clientSpecPair.getRight();
        CLIENT = clientSpecPair.getLeft();
    }

}
