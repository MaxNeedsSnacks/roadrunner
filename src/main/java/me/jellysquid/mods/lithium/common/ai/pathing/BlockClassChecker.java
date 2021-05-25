package me.jellysquid.mods.lithium.common.ai.pathing;

import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraftforge.common.extensions.IForgeBlock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class BlockClassChecker {

    public static final Logger LOGGER = LogManager.getLogger("RoadRunner Block AI Pathing analysis");

    private static final Map<Class<?>, Boolean> DYNAMIC_TYPE_CACHE = new ConcurrentHashMap<>();
    private static final Function<Class<?>, Boolean> DYNAMIC_TYPE_CHECKER = hasNonstandardImplementation(
            "getAiPathNodeType", BlockState.class, BlockView.class, BlockPos.class, MobEntity.class
    );
    private static final Map<Class<?>, Boolean> DYNAMIC_FIRE_CACHE = new ConcurrentHashMap<>();
    private static final Function<Class<?>, Boolean> DYNAMIC_FIRE_CHECKER = hasNonstandardImplementation(
            "isBurning", BlockState.class, BlockView.class, BlockPos.class
    );

    public static boolean shouldUseDynamicTypeCheck(Class<?> blockClass) {
        return DYNAMIC_TYPE_CACHE.computeIfAbsent(blockClass, DYNAMIC_TYPE_CHECKER);
    }

    public static boolean shouldUseDynamicBurningCheck(Class<?> blockClass) {
        return DYNAMIC_FIRE_CACHE.computeIfAbsent(blockClass, DYNAMIC_FIRE_CHECKER);
    }

    private static Function<Class<?>, Boolean> hasNonstandardImplementation(String name, Class<?>... args) {
        return blockClass -> {
            try {
                // We know the behavior of the default implementation in IForgeBlock, any other
                // implementation is impossible to reason about and needs to be called dynamically
                Method dynamicGetType = blockClass.getMethod(name, args);
                return dynamicGetType.getDeclaringClass() != IForgeBlock.class;
            } catch (ReflectiveOperationException | RuntimeException e) {
                // Most likely means someone forgot to add their environment annotations
                final String erroredClass = blockClass.getName();

                LOGGER.warn("Block Class {} could not be analysed: {}", erroredClass, e.getMessage());
                LOGGER.warn("Assuming the worst outcome, we're not going to override any behaviour here!");
                LOGGER.warn("(If the class above belongs to a mod, they probably forgot to annotate their client-only code).");
                return true;
            } catch (Throwable e) {
                // It's likely that someone forgot to add their environment annotations,
                // but either way we should assume the worst (non-crashing) outcome here!
                final String erroredClass = blockClass.getName();

                LOGGER.warn("Block Class {} could not be analysed because of a {}!" +
                        " Assuming the worst outcome, we're not going to override any behaviour here.", erroredClass, e.toString());
                LOGGER.warn("(If the class above belongs to a mod, they probably forgot to annotate their client-only code).");
                return true;
            }
        };
    }
}
