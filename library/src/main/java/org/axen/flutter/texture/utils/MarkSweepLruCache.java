package org.axen.flutter.texture.utils;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 标记-清除缓存类
 */
public class MarkSweepLruCache<K, V> {
    private final HashMap<K, MarkSweepEntity<V>> map;

    public MarkSweepLruCache() {
        map = new HashMap<>();
    }

    public final V get(K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        MarkSweepEntity<V> entity = map.get(key);

        synchronized (this) {
            if (entity != null) {
                entity.hitCount++;
                return entity.value;
            }
            return null;
        }
    }

    public final V put(K key, V value) {
        if (key == null || value == null) {
            throw new NullPointerException("key == null || value == null");
        }

        MarkSweepEntity<V> entity = map.get(key);
        synchronized (this) {
            if (entity != null) {
                final V oldValue = entity.value;
                if (!entryCompare(key, oldValue, value)) {
                    entity.reset(value);
                    entryRemoved(key, oldValue);
                }
                return entity.value;
            }

            MarkSweepEntity<V> createEntity = new MarkSweepEntity<>(value);
            MarkSweepEntity<V> newEntity = map.put(key, createEntity);

            if (newEntity == null) {
                return null;
            }

            return newEntity.value;
        }
    }

    public final V remove(K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        MarkSweepEntity<V> entity = map.get(key);

        if (entity == null) {
            return null;
        }

        synchronized (this) {
            if (entity.hitCount == 1) {
                MarkSweepEntity<V> remove = map.remove(key);
                if (remove != null) {
                    entryRemoved(key, remove.value);
                    remove.hitCount--;
                    return remove.value;
                }
                return null;
            }
            entity.hitCount--;
            return entity.value;
        }
    }

    public final void evictAll() {
        for (Map.Entry<K, MarkSweepEntity<V>> entry : map.entrySet()) {
            K key = entry.getKey();
            MarkSweepEntity<V> entity = map.get(key);
            if (entity != null) {
                entryRemoved(key, entity.value);
            }
        }
    }

    protected boolean entryCompare(K key, V oldValue, V value) {
        return oldValue == value;
    }

    protected void entryRemoved(K key, V value) {}

    private final static class MarkSweepEntity<T> {
        private T value;
        private int hitCount;

        public MarkSweepEntity(T value) {
            this.value = value;
            this.hitCount = 0;
        }

        private void reset(T value) {
            this.value = value;
            this.hitCount = 0;
        }
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<K, MarkSweepEntity<V>> entry : map.entrySet()) {
            builder.append("key: ").append(entry.getKey().toString()).append(", ");
            builder.append("value: ").append(entry.getValue().value.toString()).append(", ");
            builder.append("hitCount: ").append(entry.getValue().hitCount);
            builder.append("\n");
        }
        return builder.toString();
    }
}
