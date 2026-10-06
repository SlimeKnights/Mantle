package slimeknights.mantle.registration.object;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents an object which is a map of an enum to entry
 * @param <T>  Enum type
 * @param <R>  Registry type
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class EnumObject<T extends Enum<T>, R> implements MultiObject.Holders<R> {
  /** Singleton empty object, type does not matter as it has no items */
  @SuppressWarnings({"rawtypes", "unchecked"})
  private static final EnumObject EMPTY = new EnumObject(Collections.emptyMap());

  /** Internal backing supplier map */
  private final Map<T, Holder<R>> map;

  /**
   * Gets the holder for the given value.
   * @param value  Enum value
   * @return  Holder
   */
  @Nullable
  public Holder<R> getHolder(T value) {
    return map.get(value);
  }

  /**
   * Gets the value for the given enum, assuring its not null
   * @param value  Value to get
   * @return  Value
   * @throws NoSuchElementException  If the key is not defined
   * @throws NullPointerException    If the supplier at the key returns null
   */
  public R get(T value) {
    Holder<R> holder = map.get(value);
    if (holder == null) {
      throw new NoSuchElementException("Missing key " + value);
    }
    return holder.value();
  }

  /**
   * Gets the value for the given enum, or null if the key is missing
   * @param value  Key to get
   * @return  Value, or null if missing
   */
  @Nullable
  public R getOrNull(T value) {
    Holder<R> holder = map.get(value);
    if (holder == null) {
      return null;
    }
    return holder.value();
  }


  /* Values */

  /** Gets all present keys for this object */
  public Collection<T> keys() {
    return this.map.keySet();
  }

  /** Get all present holders for this object */
  @Override
  public Collection<Holder<R>> holders() {
    return this.map.values();
  }

  /** Gets the set of entries for this object */
  public Collection<Entry<T,Holder<R>>> entries() {
    return this.map.entrySet();
  }

  /**
   * Checks if this enum object contains the given value.
   * @param value  Value to check for
   * @return  True if the value is contained, false otherwise
   */
  public boolean contains(Object value) {
    return stream().anyMatch(value::equals);
  }

  /**
   * Runs the given consumer on each key in the enum object.
   * @param consumer  Consumer passed each key holder pair
   */
  public void forEachHolder(BiConsumer<T, ? super Holder<R>> consumer) {
    this.map.forEach(consumer);
  }

  /**
   * Runs the given consumer on each key in the enum object.
   * Will ignore any suppliers that have not yet resolved, to work around an error with registry events failing (which may or may not still be a thing).
   * @param consumer  Consumer passed each key value pair
   */
  public void forEach(BiConsumer<T, ? super R> consumer) {
    this.map.forEach((key, holder) -> {
      if (holder.isBound()) {
        consumer.accept(key, holder.value());
      }
    });
  }


  /* Builders */

  /**
   * Fetches the empty enum object, cast to the given type. This is useful to reduce potential of null pointers by default fields to empty map
   * @param <T>  Key type
   * @param <R>  Registry type
   * @return  Empty EnumObject
   */
  @SuppressWarnings("unchecked")
  public static <T extends Enum<T>, R> EnumObject<T,R> empty() {
    return (EnumObject<T,R>) EMPTY;
  }

  /** Creates a new builder instance */
  public static <T extends Enum<T>, R, I extends R> EnumObject.Builder<T,R> builder(Class<T> clazz) {
    return new Builder<>(clazz);
  }

  /**
   * Registers an item with multiple variants, prefixing the name with the value name
   * @param values      Enum values to use for this block
   * @param register    Function to register an entry
   * @return  EnumObject mapping between different block types
   */
  public static <E extends Enum<E>,R> EnumObject<E,R> generate(E[] values, Function<E,@Nullable Holder<R>> register) {
    if (values.length == 0) {
      throw new IllegalArgumentException("Must have at least one value");
    }
    EnumObject.Builder<E,R> builder = new EnumObject.Builder<>(values[0].getDeclaringClass());
    for (E value : values) {
      Holder<R> holder = register.apply(value);
      if (holder != null) {
        builder.put(value, holder);
      }
    }
    return builder.build();
  }

  /**
   * Enum object builder, to more conveiently create it from items, a map, or another enum object
   * @param <T>  Enum type
   * @param <R>  Registry type
   */
  @SuppressWarnings({"UnusedReturnValue", "unused"})
  public static class Builder<T extends Enum<T>, R> {
    private final Map<T,Holder<R>> map;
    public Builder(Class<T> clazz) {
      this.map = new EnumMap<>(clazz);
    }

    /**
     * Adds the given key and value to the object
     * @param key    Key
     * @param value  Value
     * @return  Builder instance
     */
    public Builder<T,R> put(T key, Holder<R> value) {
      this.map.put(key, value);
      return this;
    }

    /**
     * Adds all values from the given map
     * @param map  Map
     * @return  Builder instance
     */
    public Builder<T,R> putAll(Map<T, Holder<R>> map) {
      this.map.putAll(map);
      return this;
    }

    /**
     * Adds all values from the given enum object
     * @param object  Enum object
     * @return  Builder instance
     */
    public Builder<T,R> putAll(EnumObject<T,R> object) {
      this.map.putAll(object.map);
      return this;
    }

    /**
     * Creates the final enum object
     * @return  Constructed enum object
     */
    public EnumObject<T,R> build() {
      return new EnumObject<>(map);
    }
  }
}
