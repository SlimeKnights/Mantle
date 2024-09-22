package slimeknights.mantle.util.typed;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Interface for a map where keys are typed so the resulting value is typed. This interface is for a read only map, see {@link MutableTypedMap} for a modifiable variant.
 */
public interface TypedMap {
  /** Gets the number of entries in the map */
  int size();

  /** Checks if the map has no values */
  boolean isEmpty();

  /** Checks if the map contains the given key */
  boolean containsKey(Key<?> key);

  /** Gets the of the type of key from the map, or the default value if missing */
  @Nullable
  <R, K extends R> R getOrDefault(Key<K> key, @Nullable R defaultValue);

  /** Gets the value from the map, or null if missing */
  @Nullable
  default <K> K get(Key<K> key) {
    return getOrDefault(key, null);
  }

  /** Gets a set of all keys in the map */
  Set<Key<?>> keySet();

  /**
   * Interface for a typed key
   * @param <K>  Type of the return value from the map
   */
  @SuppressWarnings("unused")  // Key is used by the typed map for validation
  interface Key<K> {}


  /** Empty instance */
  TypedMap EMPTY = new TypedMap() {
    @Override
    public int size() {
      return 0;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public boolean containsKey(Key<?> key) {
      return false;
    }

    @Nullable
    @Override
    public <R, K extends R> R getOrDefault(Key<K> key, @Nullable R defaultValue) {
      return null;
    }

    @Override
    public Set<Key<?>> keySet() {
      return Set.of();
    }
  };

  /** Gets an empty map for the given type */
  static TypedMap empty() {
    return EMPTY;
  }
}
