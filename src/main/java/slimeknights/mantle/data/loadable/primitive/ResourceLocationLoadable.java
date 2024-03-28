package slimeknights.mantle.data.loadable.primitive;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.Loadables;

/**
 * Helper for the common case of making a string loadable that uses resource locations.
 * @param <T>
 * @see Loadables#RESOURCE_LOCATION
 */
public interface ResourceLocationLoadable<T> extends StringLoadable<T> {
  /**
   * Converts this value from a resource location.
   * @param name   Location to parse
   * @param key    Json key containing the value used for exceptions only.
   * @return  Converted value.'
   * @throws com.google.gson.JsonSyntaxException  If no value exists for that key
   */
  T fromKey(ResourceLocation name, String key);

  @Override
  default T parseString(String value, String key) {
    return fromKey(Loadables.RESOURCE_LOCATION.parseString(value, key), key);
  }

  @Override
  default T convert(JsonElement element, String key) {
    return fromKey(Loadables.RESOURCE_LOCATION.convert(element, key), key);
  }

  /**
   * Converts this object to its serialized representation.
   * @param object  Object to serialize
   * @return  String representation of the object.
   * @throws RuntimeException  if unable to serialize this to a string
   */
  ResourceLocation getKey(T object);

  @Override
  default String getString(T object) {
    return getKey(object).toString();
  }
}
