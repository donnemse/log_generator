package com.yuganji.generator.model;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * High-performance Map implementation using parallel arrays.
 * Eliminates HashMap allocation overhead for log event generation.
 * Thread-safe for read after construction; keys are shared across events.
 */
public class LogEvent extends AbstractMap<String, Object> {

    private final String[] keys;
    private final Object[] values;
    private final Map<String, Integer> keyIndex;

    /**
     * @param keys shared key array (immutable, from logger template)
     * @param keyIndex shared key-to-index mapping (immutable, from logger template)
     */
    public LogEvent(String[] keys, Map<String, Integer> keyIndex) {
        this.keys = keys;
        this.values = new Object[keys.length];
        this.keyIndex = keyIndex;
    }

    /**
     * O(1) index-based put for use during generation.
     */
    public void putByIndex(int index, Object value) {
        this.values[index] = value;
    }

    @Override
    public Object put(String key, Object value) {
        Integer idx = keyIndex.get(key);
        if (idx != null) {
            Object old = values[idx];
            values[idx] = value;
            return old;
        }
        throw new IllegalArgumentException("Unknown key: " + key);
    }

    @Override
    public Object get(Object key) {
        Integer idx = keyIndex.get(key);
        if (idx != null) {
            return values[idx];
        }
        return null;
    }

    @Override
    public boolean containsKey(Object key) {
        return keyIndex.containsKey(key);
    }

    @Override
    public int size() {
        return keys.length;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return new EntrySet();
    }

    private class EntrySet extends AbstractSet<Entry<String, Object>> {
        @Override
        public Iterator<Entry<String, Object>> iterator() {
            return new Iterator<Entry<String, Object>>() {
                private int index = 0;

                @Override
                public boolean hasNext() {
                    return index < keys.length;
                }

                @Override
                public Entry<String, Object> next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    int i = index++;
                    return new SimpleEntry<>(keys[i], values[i]);
                }
            };
        }

        @Override
        public int size() {
            return keys.length;
        }
    }
}
