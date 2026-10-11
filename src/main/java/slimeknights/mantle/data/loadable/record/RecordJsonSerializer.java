package slimeknights.mantle.data.loadable.record;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import slimeknights.mantle.data.loadable.CompositeLoadable;
import slimeknights.mantle.data.loadable.SimpleJsonSerializer;
import slimeknights.mantle.data.loadable.mapping.DispatchLoadable;
import slimeknights.mantle.util.typed.TypedMap;

/** Building block for {@link RecordLoadable}, handling the serializing. Used in {@link CompositeLoadable} and {@link DispatchLoadable} */
@FunctionalInterface
public interface RecordJsonSerializer<T> extends SimpleJsonSerializer<T> {
  /** Writes this object to json */
  void serializeInto(T object, JsonObject json, TypedMap context);

  /** same as {@link #serializeInto(Object, JsonObject, TypedMap)} but passes {@link TypedMap#EMPTY} as context. */
  @NonExtendable
  default void serializeInto(T object, JsonObject json) {
    serializeInto(object, json, TypedMap.EMPTY);
  }

  @Override
  default JsonElement serialize(T object, TypedMap context) {
    JsonObject json = new JsonObject();
    serializeInto(object, json, context);
    return json;
  }
}
