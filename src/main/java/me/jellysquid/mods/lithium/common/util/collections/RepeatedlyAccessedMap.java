package me.jellysquid.mods.lithium.common.util.collections;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * A simple wrapper around a map, optimized for repeated accesses to the same element by storing the value corresponding
 * to the last queried key. Also supports using a custom hash strategy.
 */
public class RepeatedlyAccessedMap<K, V> {
    private final Map<K, V> storage;
    private final Hash.Strategy<K> hash;
    private K lastKey;
    private V lastValue;

    public RepeatedlyAccessedMap(Hash.Strategy<K> hash) {
        this.storage = new Object2ObjectOpenCustomHashMap<>(hash);
        this.hash = hash;
    }

    public V get(K key) {
        Objects.requireNonNull(key);
        if (hash.equals(lastKey, key)) {
            return lastValue;
        }
        V result = storage.get(key);
        recordAccess(key, result);
        return result;
    }

    public V computeIfAbsent(K key, Function<K, V> makeNew) {
        Objects.requireNonNull(key);
        if (lastValue != null && hash.equals(lastKey, key)) {
            return lastValue;
        }
        V result = storage.computeIfAbsent(key, makeNew);
        recordAccess(key, result);
        return result;
    }

    private void recordAccess(K key, V value) {
        lastKey = key;
        lastValue = value;
    }
}
