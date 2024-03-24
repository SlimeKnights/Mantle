package slimeknights.mantle.util.typed;

import java.util.function.Supplier;

/**
 * Extension of {@link TypedMap} for a map that can be actively modified
 */
public interface MutableTypedMap<T> extends TypedMap<T> {
  /** Adds the given value to the map */
  <K extends T> void put(Key<K> key, K value);

  /** Gets the value from the map, computing it using the key if absent */
  default <K extends T> K computeIfAbsent(ComputingKey<K> key) {
    K value = get(key);
    if (value == null) {
      value = key.get();
      put(key, value);
    }
    return value;
  }

  /** Removes the entry associated with the given key */
  void remove(Key<? extends T> key);

  /** Removes all keys from the map */
  void clear();

  /** Key which has a value to compute if missing */
  interface ComputingKey<K> extends Key<K>, Supplier<K> {}
}
