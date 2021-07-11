package me.jellysquid.mods.lithium.common.fluid;

import com.google.common.collect.ImmutableMap;
import me.jellysquid.mods.lithium.common.RoadRunner;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = RoadRunner.MODID)
public class RequiredTagsByName {
    private static Map<Identifier, Tag.Identified<Fluid>> REQUIRED_TAGS_BY_NAME;

    @SubscribeEvent
    public static void onTagsUpdate(TagsUpdatedEvent.VanillaTagTypes ev) {
        REQUIRED_TAGS_BY_NAME = null;
    }

    public static Map<Identifier, Tag.Identified<Fluid>> getRequiredTagsByName() {
        if (REQUIRED_TAGS_BY_NAME == null) {
            Map<Identifier, Tag.Identified<Fluid>> builder = new HashMap<>();
            for (Tag.Identified<Fluid> tag : FluidTags.getRequiredTags()) {
                // putIfAbsent: Some tags (may) occur more than once, e.g. milk. In this case we are interested in the
                // first occurrence in the list
                builder.putIfAbsent(tag.getId(), tag);
            }
            REQUIRED_TAGS_BY_NAME = ImmutableMap.copyOf(builder);
        }
        return REQUIRED_TAGS_BY_NAME;
    }
}
