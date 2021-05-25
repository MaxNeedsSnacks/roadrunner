package me.jellysquid.mods.lithium.common.entity;

import it.unimi.dsi.fastutil.objects.Reference2ByteOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Class for grouping Entity classes by some property for use in TypeFilterableList
 * It is intended that an EntityClassGroup acts as if it was immutable, however we cannot predict which subclasses of
 * Entity might appear. Therefore we evaluate whether a class belongs to the class group when it is first seen.
 * Once a class was evaluated the result of it is cached and cannot be changed.
 *
 * @author 2No2Name
 */
public class EntityClassGroup {
    public static final EntityClassGroup BOAT_SHULKER_LIKE_COLLISION; //aka entities that other entities will do block-like collisions with when moving
    public static final EntityClassGroup MINECART_BOAT_LIKE_COLLISION; //aka entities that will attempt to collide with all other entities when moving

    public static final Logger LOGGER = LogManager.getLogger("RoadRunner EntityClassGroup");

    static {
        // TODO: re-evaluate life choices (and fix this)
        String remapped_method_30948 = ObfuscationReflectionHelper.findMethod(Entity.class, "func_241845_aY").getName();
        BOAT_SHULKER_LIKE_COLLISION = new EntityClassGroup(
                (Class<?> entityClass) -> isMethodFromSuperclassOverwritten(entityClass, Entity.class, remapped_method_30948));

        String remapped_method_30949 = ObfuscationReflectionHelper.findMethod(Entity.class, "func_241849_j", Entity.class).getName();
        MINECART_BOAT_LIKE_COLLISION = new EntityClassGroup(
                (Class<?> entityClass) -> isMethodFromSuperclassOverwritten(entityClass, Entity.class, remapped_method_30949, Entity.class));

        //sanity check: in case intermediary mappings changed, we fail
        if ((!MINECART_BOAT_LIKE_COLLISION.contains(MinecartEntity.class))) {
            throw new AssertionError();
        }
        if ((!BOAT_SHULKER_LIKE_COLLISION.contains(ShulkerEntity.class))) {
            throw new AssertionError();
        }
        if ((MINECART_BOAT_LIKE_COLLISION.contains(ShulkerEntity.class))) {
            //should not throw an Error here, because another mod *could* add the method to ShulkerEntity. Wwarning when this sanity check fails.
            LOGGER.warn("Either chunk.entity_class_groups is broken or something else gave Shulkers the minecart-like collision behavior.");
        }
        BOAT_SHULKER_LIKE_COLLISION.clear();
        MINECART_BOAT_LIKE_COLLISION.clear();
    }

    private final Predicate<Class<?>> classFitEvaluator;
    private volatile Reference2ByteOpenHashMap<Class<?>> class2GroupContains;

    public EntityClassGroup(Predicate<Class<?>> classFitEvaluator) {
        this.class2GroupContains = new Reference2ByteOpenHashMap<>();
        Objects.requireNonNull(classFitEvaluator);
        this.classFitEvaluator = classFitEvaluator;
    }

    public void clear() {
        this.class2GroupContains = new Reference2ByteOpenHashMap<>();
    }

    public boolean contains(Class<?> entityClass) {
        byte contains = this.class2GroupContains.getOrDefault(entityClass, (byte) 2);
        if (contains != 2) {
            return contains == 1;
        } else {
            return this.testAndAddClass(entityClass);
        }
    }

    private boolean testAndAddClass(Class<?> entityClass) {
        byte contains;
        //synchronizing here to avoid multiple threads replacing the map at the same time, and therefore possibly undoing progress
        //it could also be fixed by using an AtomicReference's CAS, but we are writing very rarely (less than 150 times for the total game runtime in vanilla)
        synchronized (this) {
            //test the same condition again after synchronizing, as the collection might have been updated while this thread blocked
            contains = this.class2GroupContains.getOrDefault(entityClass, (byte) 2);
            if (contains != 2) {
                return contains == 1;
            }
            //construct new map instead of updating the old map to avoid thread safety problems
            //the map is not modified after publication
            Reference2ByteOpenHashMap<Class<?>> newMap = this.class2GroupContains.clone();
            contains = this.classFitEvaluator.test(entityClass) ? (byte) 1 : (byte) 0;
            newMap.put(entityClass, contains);
            //publish the new map in a volatile field, so that all threads reading after this write can also see all changes to the map done before the write
            this.class2GroupContains = newMap;
        }
        return contains == 1;
    }

    public static boolean isMethodFromSuperclassOverwritten(Class<?> clazz, Class<?> superclass, String methodName, Class<?>... methodArgs) {
        if (clazz != null && clazz != superclass && superclass.isAssignableFrom(clazz)) {
            try {
                Method m = clazz.getMethod(methodName, methodArgs);
                return m.getDeclaringClass() != superclass;
            } catch (Throwable e) {
                // It's likely that someone forgot to add their environment annotations,
                // but either way we should assume the worst (non-crashing) outcome here!
                final String erroredClass = clazz.getName();

                LOGGER.warn("Entity Class {} could not be analysed because of a {}!" +
                        " Assuming the worst outcome, we're not going to override any behaviour here.", erroredClass, e.toString());
                LOGGER.warn("(If the class above belongs to a mod, they probably forgot to annotate their client-only code).");
                return true;
            }
        }
        return false;
    }
}