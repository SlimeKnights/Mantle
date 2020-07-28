package slimeknights.mantle.registration.object;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.IRegistryDelegate;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Represents an object which is a map of an enum to entry
 * @param <T>  Enum type
 * @param <I>  Entry type
 */
@SuppressWarnings("unused")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class EnumObject<T extends Enum<T>, I extends IForgeRegistryEntry<? super I>> {
  /** Singleton empty object, type does not matter as it has no items */
  private static final EnumObject EMPTY = new EnumObject<>(Collections.emptyMap());

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
   * Gets the value for the given enum
   * @param value  Value to get
   * @return  Value, or null if missing
   */
  @Nullable
  public I get(T value) {
    Supplier<? extends I> supplier = map.get(value);
    if (supplier == null) {
      return null;
    }
    return supplier.get();
  }

  /**
   * Checks if this enum object contains the given value
   * @param value  Value to check for
   * @return  True if the value is contained, false otherwise
   */
  public boolean contains(IForgeRegistryEntry<? super I> value) {
    return this.map.values().stream().map(Supplier::get).anyMatch(value::equals);
  }

  /**
   * Gets a list of values in this enum object
   * @return  List of values in the object
   */
  public List<I> values() {
    return this.map.values().stream().map(Supplier::get).collect(Collectors.toList());
  }

  /**
   * Runs the given consumer on each key in the enum object
   * @param consumer  Consumer passed each key value pair
   */
  public void forEach(BiConsumer<T, I> consumer) {
    this.map.forEach((key, sup) -> consumer.accept(key, sup.get()));
  }

  /**
   * Fetches the empty enum object, casted to the given type. This is useful to reduce potential of null pointers by default fields to empty map
   * @param <T>  Key type
   * @param <I>  Value type
   * @return  Empty EnumObject
   */
  @SuppressWarnings("unchecked")
  public static <T extends Enum<T>, I extends IForgeRegistryEntry<? super I>> EnumObject<T,I> empty() {
    return (EnumObject<T,I>) EMPTY;
  }

  /**
   * Enum object builder, to more conveiently create it from items, a map, or another enum object
   * @param <T>  Enum type
   * @param <I>  Entry type
   */
  @SuppressWarnings({"UnusedReturnValue", "unused"})
  public static class Builder<T extends Enum<T>, I extends IForgeRegistryEntry<? super I>> {
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
    @SuppressWarnings("unchecked")
    public Builder<T,I> putDelegate(T key, IRegistryDelegate<? super I> value) {
      this.map.put(key, () -> (I) value.get());
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
