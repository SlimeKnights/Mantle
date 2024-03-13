package slimeknights.mantle.registration.object;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Represents an object which is a map of an enum to entry
 * @param <T>  Enum type
 * @param <I>  Entry type
 */
@SuppressWarnings({"unused", "WeakerAccess", "ClassCanBeRecord"})
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class EnumObject<T extends Enum<T>, I> {
  /** Singleton empty object, type does not matter as it has no items */
  @SuppressWarnings({"rawtypes", "unchecked"})
  private static final EnumObject EMPTY = new EnumObject(Collections.emptyMap());

  /** Internal backing supplier map */
  private final Map<T,Supplier<? extends I>> map;

  /**
   * Gets a entry supplier for the given value
   * @param value  Value to get
   * @return  Entry supplier
   */
  @Nullable
  public Supplier<? extends I> getSupplier(T value) {
    return map.get(value);
  }

  /**
   * Gets the value for the given enum, assuring its not null
   * @param value  Value to get
   * @return  Value
   * @throws NoSuchElementException  If the key is not defined
   * @throws NullPointerException    If the supplier at the key returns null
   */
  public I get(T value) {
    Supplier<? extends I> supplier = map.get(value);
    if (supplier == null) {
      throw new NoSuchElementException("Missing key " + value);
    }
    return Objects.requireNonNull(supplier.get(), () -> "No enum object value for " + value);
  }

  /**
   * Gets the value for the given enum, or null if the key is missing
   * @param value  Key to get
   * @return  Value, or null if missing
   */
  @Nullable
  public I getOrNull(T value) {
    Supplier<? extends I> supplier = map.get(value);
    if (supplier == null) {
      return null;
    }
    try {
      return supplier.get();
    } catch (NullPointerException e) {
      return null;
    }
  }

  /**
   * Checks if this enum object contains the given value.
   * @param value  Value to check for
   * @return  True if the value is contained, false otherwise
   */
  public boolean contains(Object value) {
    return this.map.values().stream().map(Supplier::get).anyMatch(value::equals);
  }

  /**
   * Gets a list of values in this enum object. Will error if a {@link net.minecraftforge.registries.RegistryObject} cannot be resolved, unlike {@link #forEach(Consumer)}
   * @return  List of values in the object
   */
  public List<I> values() {
    return this.map.values().stream().map(Supplier::get).filter(Objects::nonNull).collect(Collectors.toList());
  }

  /**
   * Runs the given consumer on each key in the enum object.
   * Will ignore any suppliers that have not yet resolved, to work around a Forge error with registry events failing.
   * @param consumer  Consumer passed each key value pair
   */
  public void forEach(BiConsumer<T, ? super I> consumer) {
    this.map.forEach((key, sup) -> {
      I value;
      try {
        value = sup.get();
      } catch (NullPointerException e) {
        // registry object throws null pointer exception on get if the object is not registered, ignore
        return;
      }
      if (value != null) {
        consumer.accept(key, value);
      }
    });
  }

  /**
   * Runs the given consumer on each key in the enum object.
   * Will ignore any suppliers that have not yet resolved, to work around a Forge error with registry events failing.
   * @param consumer  Consumer passed each key value pair
   */
  public void forEach(Consumer<? super I> consumer) {
    forEach((k, v) -> consumer.accept(v));
  }

  /**
   * Fetches the empty enum object, casted to the given type. This is useful to reduce potential of null pointers by default fields to empty map
   * @param <T>  Key type
   * @param <I>  Value type
   * @return  Empty EnumObject
   */
  @SuppressWarnings("unchecked")
  public static <T extends Enum<T>, I> EnumObject<T,I> empty() {
    return (EnumObject<T,I>) EMPTY;
  }

  /**
   * Enum object builder, to more conveiently create it from items, a map, or another enum object
   * @param <T>  Enum type
   * @param <I>  Entry type
   */
  @SuppressWarnings({"UnusedReturnValue", "unused"})
  public static class Builder<T extends Enum<T>, I> {
    private final Map<T, Supplier<? extends I>> map;
    public Builder(Class<T> clazz) {
      this.map = new EnumMap<>(clazz);
    }

    /**
     * Adds the given key and value to the object
     * @param key    Key
     * @param value  Value
     * @return  Builder instance
     */
    public Builder<T,I> put(T key, Supplier<? extends I> value) {
      this.map.put(key, value);
      return this;
    }

    /**
     * Adds the given registry delegate to this enum object.
     * This method does an unchecked cast to add the object, so be absolutely certain the class it right.
     * @param key    Key
     * @param value  Registry delegate
     * @return  Builder instance
     */
    public Builder<T,I> put(T key, I value) {
      // TODO: should we use holders? is there a practical way to fetch one?
      this.map.put(key, () -> value);
      return this;
    }

    /**
     * Adds all values from the given map
     * @param map  Map
     * @return  Builder instance
     */
    public Builder<T,I> putAll(Map<T, Supplier<? extends I>> map) {
      this.map.putAll(map);
      return this;
    }

    /**
     * Adds all values from the given enum object
     * @param object  Enum object
     * @return  Builder instance
     */
    public Builder<T,I> putAll(EnumObject<T,? extends I> object) {
      this.map.putAll(object.map);
      return this;
    }

    /**
     * Creates the final enum object
     * @return  Constructed enum object
     */
    public EnumObject<T,I> build() {
      return new EnumObject<>(map);
    }
  }
}
