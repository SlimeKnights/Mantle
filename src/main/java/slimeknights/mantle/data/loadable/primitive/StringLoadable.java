package slimeknights.mantle.data.loadable.primitive;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.mapping.MapLoadable;
import slimeknights.mantle.data.loadable.mapping.MappedLoadable;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Loadable that maps to a string, can be used as a key for a {@link com.google.gson.JsonObject} parsed as a {@link java.util.Map}.
 * @param <T>
 */
public interface StringLoadable<T> extends Loadable<T> {
  /** Loadable for the default max string length */
  StringLoadable<String> DEFAULT = maxLength(Short.MAX_VALUE);

  /** Creates a new string loadable with the given max length */
  static StringLoadable<String> maxLength(int maxLength) {
    return new MaxLengthStringLoadable(maxLength);
  }

  /**
   * Converts this value from a string.
   * @param value  Value to parse
   * @param key    Json key containing the value used for exceptions only.
   * @return  Converted value.'
   * @throws com.google.gson.JsonSyntaxException  If unable to parse the value
   */
  T parseString(String value, String key);

  @Override
  default T convert(JsonElement element, String key) {
    return parseString(GsonHelper.convertToString(element, key), key);
  }

  /**
   * Converts this object to its serialized representation.
   * @param object  Object to serialize
   * @return  String representation of the object.
   * @throws RuntimeException  if unable to serialize this to a string
   */
  String getString(T object);

  @Override
  default JsonElement serialize(T object) {
    return new JsonPrimitive(getString(object));
  }


  /* Mapping - switches to the string version of the methods */

  /**
   * Creates a map loadable with this as the key
   * @param valueLoadable  Loadable for the map values
   * @param minSize        Min size of the map
   * @param <V>  Map value type
   * @return  Map loadable
   */
  default <V> Loadable<Map<T,V>> mapWithValues(Loadable<V> valueLoadable, int minSize) {
    return new MapLoadable<>(this, valueLoadable, minSize);
  }

  /**
   * Creates a map loadable with this as the key with a min size of 1
   * @param valueLoadable  Loadable for the map values
   * @param <V>  Map value type
   * @return  Map loadable
   */
  default <V> Loadable<Map<T,V>> mapWithValues(Loadable<V> valueLoadable) {
    return mapWithValues(valueLoadable, 1);
  }

  @Override
  default <M> StringLoadable<M> xmap(BiFunction<T,ErrorFactory,M> from, BiFunction<M,ErrorFactory,T> to) {
    return MappedLoadable.of(this, from, to);
  }

  @Override
  default <M> StringLoadable<M> comapFlatMap(BiFunction<T,ErrorFactory,M> from, Function<M,T> to) {
    return xmap(from, MappedLoadable.flatten(to));
  }

  @Override
  default <M> StringLoadable<M> flatComap(Function<T,M> from, BiFunction<M,ErrorFactory,T> to) {
    return xmap(MappedLoadable.flatten(from), to);
  }

  @Override
  default <M> StringLoadable<M> flatXmap(Function<T,M> from, Function<M,T> to) {
    return xmap(MappedLoadable.flatten(from), MappedLoadable.flatten(to));
  }

  @Override
  default Loadable<T> validate(BiFunction<T,ErrorFactory,T> validator) {
    return xmap(validator, validator);
  }
}
