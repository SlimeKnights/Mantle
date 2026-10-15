package slimeknights.mantle.data.loadable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Contract;
import slimeknights.mantle.data.loadable.mapping.DispatchLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;

/** Building block for {@link Loadable}, handling the deserializing. Used in {@link CompositeLoadable} and {@link DispatchLoadable} */
@FunctionalInterface
public interface SimpleJsonDeserializer<T> {
  /**
   * Deserializes the object from the passed JSON element
   * @param element  Element of an unknown type to parse
   * @param key      Key that contained this element
   * @param context  Additional parsing context, used notably by recipe serializers to store the ID and serializer.
   * @return  Parsed loadable value
   * @throws com.google.gson.JsonSyntaxException  If unable to read from JSON
   */
  T convert(JsonElement element, String key, TypedMap context);

  /** Same as {@link #convert(JsonElement, String, TypedMap)} but passes {@link TypedMap#EMPTY} for context. */
  @NonExtendable
  default T convert(JsonElement element, String key) {
    return convert(element, key, TypedMap.EMPTY);
  }


  /* Helpers for raw loadable use */

  /**
   * Gets then deserializes the given fieldm throwing if it is missing
   * You should not override this method as we wish to leave that handling missing up to the RecordLoadable.
   * Instead, consider a custom implementation of defaultField if you have a standard default.
   * @param parent  Parent to fetch field from
   * @param key     Field to get
   * @param context Additional parsing context, used notably by recipe serializers to store the ID and serializer.
   * @return  Value, or throws if missing
   * @throws JsonSyntaxException  If the field is missing or cannot be parsed.
   */
  @NonExtendable
  default T getIfPresent(JsonObject parent, String key, TypedMap context) {
    if (parent.has(key)) {
      return convert(parent.get(key), key, context);
    }
    throw new JsonSyntaxException("Missing JSON field '" + key + "'");
  }

  /** Same as {@link #getIfPresent(JsonObject, String, TypedMap)} but passes {@link TypedMap#EMPTY} for the context */
  @NonExtendable
  default T getIfPresent(JsonObject parent, String key) {
    return getIfPresent(parent, key, TypedMap.EMPTY);
  }

  /**
   * Gets then deserializes the given field, or returns a default value if its missing.
   * You should not override this method as we wish to leave that handling missing up to the RecordLoadable.
   * Instead, consider a custom implementation of defaultField if you have a standard default.
   * @param parent        Parent to fetch field from
   * @param key           Field to get
   * @param defaultValue  Default value to fetch
   * @param context       Additional parsing context, used notably by recipe serializers to store the ID and serializer.
   * @return  Value or default.
   * @throws JsonSyntaxException  If the field cannot be parsed.
   */
  @NonExtendable
  @Nullable
  @Contract("_, _, !null, _ -> !null")
  default T getOrDefault(JsonObject parent, String key, @Nullable T defaultValue, TypedMap context) {
    JsonElement element = parent.get(key);
    if (element != null && !element.isJsonNull()) {
      return convert(element, key, context);
    }
    return defaultValue;
  }

  /** Same as {@link #getOrDefault(JsonObject, String, Object, TypedMap)} but passes {@link TypedMap#EMPTY} for context. */
  @NonExtendable
  @Nullable
  @Contract("_, _, !null -> !null")
  default T getOrDefault(JsonObject parent, String key, @Nullable T defaultValue) {
    return getOrDefault(parent, key, defaultValue, TypedMap.EMPTY);
  }
}
