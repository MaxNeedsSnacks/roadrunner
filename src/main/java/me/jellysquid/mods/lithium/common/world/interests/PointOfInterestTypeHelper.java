package me.jellysquid.mods.lithium.common.world.interests;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import me.jellysquid.mods.lithium.common.RoadRunner;
import me.jellysquid.mods.lithium.mixin.ai.poi.fast_init.PointOfInterestTypeAccess;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Map;
import java.util.Set;

/**
 * Replaces the type of the blockstate to POI map with a faster collection type which uses reference equality.
 */
@EventBusSubscriber(modid = RoadRunner.MODID, bus = Bus.MOD)
public class PointOfInterestTypeHelper {
    private static Set<BlockState> TYPES;

    public static boolean shouldScan(ChunkSection section) {
        return section.hasAny(TYPES::contains);
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent ev) {
        if (TYPES != null) {
            throw new IllegalStateException("Already initialized");
        }
        Map<BlockState, PointOfInterestType> blockstatePOIMap = PointOfInterestTypeAccess.getBlockStateToPointOfInterestType();
        blockstatePOIMap = new Reference2ReferenceOpenHashMap<>(blockstatePOIMap);
        PointOfInterestTypeAccess.setBlockStateToPointOfInterestType(blockstatePOIMap);

        // This threshold is not based on anything but gut feeling, if anyone has actual profiling data it should be
        // adjusted
        if (blockstatePOIMap.size() < 20) {
            TYPES = new ReferenceArraySet<>(blockstatePOIMap.keySet());
        } else {
            TYPES = new ReferenceOpenHashSet<>(blockstatePOIMap.keySet());
        }
    }
}
