package me.jellysquid.mods.lithium.common.config;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Documentation of these options: https://github.com/jellysquid3/lithium-fabric/wiki/Configuration-File
 */
@SuppressWarnings("CanBeFinal")
public class RoadRunnerConfig {
    private static final Logger LOGGER = LogManager.getLogger("RoadRunner Config");

    private final Map<String, Option> options = new HashMap<>();

    private RoadRunnerConfig() {
        // Defines the default rules which can be configured by the user or other mods.
        // You must manually add a rule for any new mixins not covered by an existing package rule.

        this.addMixinRule("ai", true);
        this.addMixinRule("ai.goal", true);
        this.addMixinRule("ai.nearby_entity_tracking", true);
        this.addMixinRule("ai.nearby_entity_tracking.goals", true);
        this.addMixinRule("ai.pathing", true);
        this.addMixinRule("ai.poi", true);
        this.addMixinRule("ai.poi.fast_init", true);
        this.addMixinRule("ai.poi.fast_retrieval", true);
        this.addMixinRule("ai.raid", true);
        this.addMixinRule("ai.task", true);

        this.addMixinRule("alloc", true);
        this.addMixinRule("alloc.chunk_random", true);
        this.addMixinRule("alloc.chunk_ticking", true);
        this.addMixinRule("alloc.composter", true);
        this.addMixinRule("alloc.entity_tracker", true);
        this.addMixinRule("alloc.enum_values", true);
        this.addMixinRule("alloc.explosion_behavior", true);
        this.addMixinRule("alloc.world_ticking", true);

        this.addMixinRule("block", true);
        this.addMixinRule("block.flatten_states", true);
        this.addMixinRule("block.moving_block_shapes", true);

        this.addMixinRule("cached_hashcode", true);

        this.addMixinRule("chunk", true);
        this.addMixinRule("chunk.count_oversized_blocks", true);
        this.addMixinRule("chunk.entity_class_groups", true);
        this.addMixinRule("chunk.no_locking", true);
        this.addMixinRule("chunk.palette", true);
        this.addMixinRule("chunk.section_update_tracking", true);
        this.addMixinRule("chunk.serialization", true);

        this.addMixinRule("collections", true);
        this.addMixinRule("collections.entity_filtering", true);

        this.addMixinRule("entity", true);
        this.addMixinRule("entity.collisions", true);
        this.addMixinRule("entity.data_tracker", true);
        this.addMixinRule("entity.data_tracker.no_locks", true);
        this.addMixinRule("entity.data_tracker.use_arrays", true);
        this.addMixinRule("entity.fast_suffocation_check", true);
        this.addMixinRule("entity.gravity_check_block_below", true);
        this.addMixinRule("entity.inactive_navigations", true);
        this.addMixinRule("entity.replace_entitytype_predicates", true);
        this.addMixinRule("entity.skip_fire_check", true);
        this.addMixinRule("entity.stream_entity_collisions_lazily", true);

        this.addMixinRule("gen", true);
        this.addMixinRule("gen.biome_noise_cache", true);
        this.addMixinRule("gen.cached_generator_settings", true);
        this.addMixinRule("gen.chunk_region", true);
        this.addMixinRule("gen.fast_island_noise", true);
        this.addMixinRule("gen.fast_layer_sampling", true);
        this.addMixinRule("gen.fast_multi_source_biomes", true);
        this.addMixinRule("gen.fast_noise_interpolation", true);
        this.addMixinRule("gen.features", true);
        this.addMixinRule("gen.perlin_noise", true);
        this.addMixinRule("gen.voronoi_biomes", true);

        this.addMixinRule("math", true);
        this.addMixinRule("math.fast_blockpos", true);
        this.addMixinRule("math.fast_util", true);
        this.addMixinRule("math.sine_lut", true);

        this.addMixinRule("shapes", true);
        this.addMixinRule("shapes.blockstate_cache", true);
        this.addMixinRule("shapes.lazy_shape_context", true);
        this.addMixinRule("shapes.optimized_matching", true);
        this.addMixinRule("shapes.precompute_shape_arrays", true);
        this.addMixinRule("shapes.shape_merging", true);
        this.addMixinRule("shapes.specialized_shapes", true);

        this.addMixinRule("tag", true);

        this.addMixinRule("world", true);
        this.addMixinRule("world.block_entity_ticking", true);
        this.addMixinRule("world.block_entity_ticking.collections", true);
        this.addMixinRule("world.block_entity_ticking.should_tick_cache", true);
        this.addMixinRule("world.block_entity_ticking.sleeping", true);
        this.addMixinRule("world.block_entity_ticking.support_cache", true);
        this.addMixinRule("world.chunk_access", true);
        this.addMixinRule("world.chunk_inline_block_access", true);
        this.addMixinRule("world.chunk_task_system", true);
        this.addMixinRule("world.chunk_tickets", true);
        this.addMixinRule("world.chunk_ticking", true);
        this.addMixinRule("world.explosions", true);
        this.addMixinRule("world.mob_spawning", true);
        this.addMixinRule("world.player_chunk_tick", true);
        this.addMixinRule("world.tick_scheduler", true);
    }

    /**
     * Defines a Mixin rule which can be configured by users and other mods.
     *
     * @param mixin   The name of the mixin package which will be controlled by this rule
     * @param enabled True if the rule will be enabled by default, otherwise false
     * @throws IllegalStateException If a rule with that name already exists
     */
    private void addMixinRule(String mixin, boolean enabled) {
        String name = getMixinRuleName(mixin);

        if (this.options.putIfAbsent(name, new Option(name, enabled, false)) != null) {
            throw new IllegalStateException("Mixin rule already defined: " + mixin);
        }
    }

    private void readProperties(Properties props) {
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            Option option = this.options.get(key);

            if (option == null) {
                LOGGER.warn("No configuration key exists with name '{}', ignoring", key);
                continue;
            }

            boolean enabled;

            if (value.equalsIgnoreCase("true")) {
                enabled = true;
            } else if (value.equalsIgnoreCase("false")) {
                enabled = false;
            } else {
                LOGGER.warn("Invalid value '{}' encountered for configuration key '{}', ignoring", value, key);
                continue;
            }

            option.setEnabled(enabled, true);
        }
    }

    /*
     * Modified by RoadRunner for Forge mods
     *
     * Since Forge doesn't have any good ways to add overrides to the metadata files,
     * we'll just be using a properties file that goes into a mod's resources folder.
     *
     */
    private void applyModOverrides() {
        for (ModInfo mod : ModList.get().getMods()) {
            String modid = mod.getModId();
            Path path = mod.getOwningFile().getFile().findResource("roadrunner.overrides.properties");
            if (Files.exists(path)) {
                Properties props = new Properties();

                try (InputStream stream = Files.newInputStream(path)) {
                    props.load(stream);
                } catch (IOException e) {
                    LOGGER.warn("Could not load overrides file for mod '{}', ignoring", modid);
                    continue;
                }

                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    applyModOverride(modid, entry.getKey().toString(), entry.getValue().toString());
                }
            }
        }
    }

    private void applyModOverride(String modid, String name, String value) {
        Option option = this.options.get(name);

        if (option == null) {
            LOGGER.warn("Mod '{}' attempted to override option '{}', which doesn't exist, ignoring", modid, name);
            return;
        }

        boolean enabled = Boolean.parseBoolean(value);

        if (!value.equals(Boolean.toString(enabled))) {
            LOGGER.warn("Mod '{}' attempted to override option '{}' with an invalid value, ignoring", modid, name);
            return;
        }

        // disabling the option takes precedence over enabling
        if (!enabled && option.isEnabled()) {
            option.clearModsDefiningValue();
        }

        if (!enabled || option.isEnabled() || option.getDefiningMods().isEmpty()) {
            option.addModOverride(enabled, modid);
        }
    }

    /**
     * Returns the effective option for the specified class name. This traverses the package path of the given mixin
     * and checks each root for configuration rules. If a configuration rule disables a package, all mixins located in
     * that package and its children will be disabled. The effective option is that of the highest-priority rule, either
     * a enable rule at the end of the chain or a disable rule at the earliest point in the chain.
     *
     * @return Null if no options matched the given mixin name, otherwise the effective option for this Mixin
     */
    public Option getEffectiveOptionForMixin(String mixinClassName) {
        int lastSplit = 0;
        int nextSplit;

        Option rule = null;

        while ((nextSplit = mixinClassName.indexOf('.', lastSplit)) != -1) {
            String key = getMixinRuleName(mixinClassName.substring(0, nextSplit));

            Option candidate = this.options.get(key);

            if (candidate != null) {
                rule = candidate;

                if (!rule.isEnabled()) {
                    return rule;
                }
            }

            lastSplit = nextSplit + 1;
        }

        return rule;
    }

    /**
     * Loads the configuration file from the specified location. If it does not exist, a new configuration file will be
     * created. The file on disk will then be updated to include any new options.
     */
    public static RoadRunnerConfig load(File file) {
        RoadRunnerConfig config = new RoadRunnerConfig();

        if (file.exists()) {
            Properties props = new Properties();

            try (FileInputStream fin = new FileInputStream(file)) {
                props.load(fin);
            } catch (IOException e) {
                throw new RuntimeException("Could not load config file", e);
            }

            config.readProperties(props);
        } else {
            try {
                writeDefaultConfig(file);
            } catch (IOException e) {
                LOGGER.warn("Could not write default configuration file", e);
            }
        }

        config.applyModOverrides();

        return config;
    }

    private static void writeDefaultConfig(File file) throws IOException {
        File dir = file.getParentFile();

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Could not create parent directories");
            }
        } else if (!dir.isDirectory()) {
            throw new IOException("The parent file is not a directory");
        }

        try (Writer writer = new FileWriter(file)) {
            writer.write("# This is the configuration file for RoadRunner.\n");
            writer.write("# This file exists for debugging purposes and should not be configured otherwise.\n");
            writer.write("#\n");
            writer.write("# You can find information on editing this file and all the available options here:\n");
            writer.write("# https://github.com/MaxNeedsSnacks/roadrunner#configuration\n");
            writer.write("#\n");
            writer.write("# By default, this file will be empty except for this notice.\n");
        }
    }

    private static String getMixinRuleName(String name) {
        return "mixin." + name;
    }

    public int getOptionCount() {
        return this.options.size();
    }

    public int getOptionOverrideCount() {
        return (int) this.options.values()
                .stream()
                .filter(Option::isOverridden)
                .count();
    }
}
