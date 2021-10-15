package me.jellysquid.mods.lithium.mixin.ai.task;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(Task.class)
public class TaskMixin<E extends LivingEntity> {
    @Mutable
    @Shadow
    @Final
    protected Map<MemoryModuleType<?>, MemoryModuleState> requiredMemoryStates;

    @Inject(method = "<init>(Ljava/util/Map;II)V", at = @At("RETURN"))
    private void init(Map<MemoryModuleType<?>, MemoryModuleState> map, int int_1, int int_2, CallbackInfo ci) {
        this.requiredMemoryStates = new Reference2ObjectOpenHashMap<>(map);
    }

    /**
     * @reason Use fastIterable instead of standard entry set iterator
     * @author JellySquid
     */
    @Overwrite
    public boolean hasRequiredMemoryState(E entity) {
        Iterable<Reference2ObjectMap.Entry<MemoryModuleType<?>, MemoryModuleState>> iterable =
                Reference2ObjectMaps.fastIterable((Reference2ObjectOpenHashMap<MemoryModuleType<?>, MemoryModuleState>) this.requiredMemoryStates);

        for (Reference2ObjectMap.Entry<MemoryModuleType<?>, MemoryModuleState> entry : iterable) {
            if (!entity.getBrain().isMemoryInState(entry.getKey(), entry.getValue())) {
                return false;
            }
        }

        return true;
    }
}
