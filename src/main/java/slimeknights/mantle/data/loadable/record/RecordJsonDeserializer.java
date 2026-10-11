package slimeknights.mantle.data.loadable.record;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.loadable.CompositeLoadable;
import slimeknights.mantle.data.loadable.SimpleJsonDeserializer;
import slimeknights.mantle.data.loadable.mapping.DispatchLoadable;
import slimeknights.mantle.util.typed.TypedMap;

/** Building block for {@link RecordLoadable}, handling the deserializing. Used in {@link CompositeLoadable} and {@link DispatchLoadable} */
public interface RecordJsonDeserializer<T> extends SimpleJsonDeserializer<T> {
  /**
   * Deserializes the object from json.
   * @param json     JSON object
   * @param context  Additional parsing context, used notably by recipe serializers to store the ID and serializer.
   * @return  Parsed loadable value
   * @throws com.google.gson.JsonSyntaxException  If unable to read from JSON
   */
  T deserialize(JsonObject json, TypedMap context);

  /** Same as {@link #deserialize(JsonObject, TypedMap)} but uses {@link TypedMap#EMPTY} as the context. */
  default T deserialize(JsonObject json) {
    return deserialize(json, TypedMap.EMPTY);
  }

  @Override
  default T convert(JsonElement element, String key, TypedMap context) {
    return deserialize(GsonHelper.convertToJsonObject(element, key), context);
  }
}
