package me.jellysquid.mods.lithium.common;

import me.jellysquid.mods.lithium.common.config.LithiumConfig;
import net.minecraftforge.fml.common.Mod;

@Mod(LithiumMod.MODID)
public class LithiumMod {
    public static final String MODID = "lithium";

    public static LithiumConfig CONFIG;

    public LithiumMod() {
        if (CONFIG == null) {
            throw new IllegalStateException("The mixin plugin did not initialize the config! Did it not load?");
        }
    }
}
