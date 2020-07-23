package slimeknights.mantle.registration.object;

import lombok.AllArgsConstructor;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Represents an object which is a map of an enum to entry
 * @param <T>  Enum type
 * @param <I>  Entry type
 */
@AllArgsConstructor
@SuppressWarnings("WeakerAccess")
public class EnumObject<T extends Enum<T>, I extends IForgeRegistryEntry<? super I>> {
  private final Map<T,Supplier<? extends I>> map;

  /**
   * Gets a entry supplier for the given value
   * @param value  Value to get
   * @return  Entry supplier
   */
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
    if (!map.containsKey(value)) {
      return null;
    }
    return getSupplier(value).get();
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
}
