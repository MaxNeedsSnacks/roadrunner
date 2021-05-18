package me.jellysquid.mods.lithium.common.ai.pathing;

import com.sun.org.apache.xpath.internal.operations.Bool;
import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraftforge.common.extensions.IForgeBlock;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlockClassChecker {
    private static final Map<Class<?>, Boolean> NEEDS_DYNAMIC_CHECK = new ConcurrentHashMap<>();

    public static boolean shouldUseDynamicTypeCheck(Class<?> blockClass) {
        return NEEDS_DYNAMIC_CHECK.computeIfAbsent(blockClass, blockClass2 -> {
            try {
                // We know the behavior of the default implementation of getAiPathNodeType (in IForgeBlock), any other
                // implementation is impossible to reason about and needs to be called dynamically
                Method dynamicGetType = blockClass.getMethod(
                        "getAiPathNodeType", BlockState.class, BlockView.class, BlockPos.class, MobEntity.class
                );
                return dynamicGetType.getDeclaringClass() != IForgeBlock.class;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return true;
            }
        });
    }
}
