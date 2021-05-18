package me.jellysquid.mods.lithium.common.ai.pathing;

import me.jellysquid.mods.lithium.common.RoadRunner;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = RoadRunner.MODID)
public class NodeRefreshHandler {
    @SubscribeEvent
    public static void onTagsReloaded(TagsUpdatedEvent ev) {
        if (!BlockStatePathingCache.class.isAssignableFrom(BlockState.class)) {
            return;
        }
        for (Block b : ForgeRegistries.BLOCKS.getValues()) {
            // We need to compute this even for dynamic blocks, since those may still return null and fall back to
            // vanilla behavior
            for (BlockState state : b.getStateManager().getStates()) {
                ((BlockStatePathingCache) state).refreshCachedType();
            }
        }
    }
}
