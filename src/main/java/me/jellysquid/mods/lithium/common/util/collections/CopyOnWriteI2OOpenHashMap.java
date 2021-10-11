package me.jellysquid.mods.lithium.common.util.collections;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Extra assumption: Write methods are externally synchronized
 */
public class CopyOnWriteI2OOpenHashMap<V> implements Int2ObjectMap<V> {
    private volatile Int2ObjectMap<V> baseMap;

    public CopyOnWriteI2OOpenHashMap(Map<Integer, V> entries) {
        baseMap = new Int2ObjectOpenHashMap<>(entries);
    }

    @Override
    public int size() {
        return this.baseMap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.baseMap.isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
        return this.baseMap.containsValue(value);
    }

    @Override
    public void putAll(@NotNull Map<? extends Integer, ? extends V> m) {
        Int2ObjectMap<V> newMap = new Int2ObjectOpenHashMap<>(this.baseMap);
        newMap.putAll(m);
        this.baseMap = newMap;
    }

    @Override
    public void defaultReturnValue(V rv) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V defaultReturnValue() {
        return this.baseMap.defaultReturnValue();
    }

    @Override
    public ObjectSet<Entry<V>> int2ObjectEntrySet() {
        return this.baseMap.int2ObjectEntrySet();
    }

    @Override
    public IntSet keySet() {
        return this.baseMap.keySet();
    }

    @Override
    public ObjectCollection<V> values() {
        return this.baseMap.values();
    }

    @Override
    public V get(int key) {
        return this.baseMap.get(key);
    }

    @Override
    public boolean containsKey(int key) {
        return this.baseMap.containsKey(key);
    }

    @Override
    public V put(int key, V value) {
        Int2ObjectMap<V> newMap = new Int2ObjectOpenHashMap<>(this.baseMap);
        V result = newMap.put(key, value);
        this.baseMap = newMap;
        return result;
    }
}
