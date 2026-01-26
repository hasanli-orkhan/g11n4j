package info.md7.g11n4j.core.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU cache for resolved messages with context.
 * Thread-safe implementation using Collections.synchronizedMap.
 */
public class MessageCache {

    private final Map<String, String> cache;
    private final int maxSize;

    public MessageCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be positive");
        }
        this.maxSize = maxSize;
        this.cache = java.util.Collections.synchronizedMap(
            new LinkedHashMap<String, String>(maxSize + 1, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                    return size() > MessageCache.this.maxSize;
                }
            }
        );
    }

    public void put(String key, String value) {
        cache.put(key, value);
    }

    public String get(String key) {
        return cache.get(key);
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }
}
