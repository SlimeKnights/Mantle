package slimeknights.mantle.data.loadable.field;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import slimeknights.mantle.data.loadable.Loadable;

import java.util.Map.Entry;
import java.util.function.Function;

/**
 * Field that tries to save the object directly, but falls back to saving it in a key if unable
 * @param <T>  Field type
 * @param <P>  Parent type
 */
public record TryDirectField<T,P>(Loadable<T> loadable, String key, Function<P,T> getter, String... conflicts) implements AlwaysPresentLoadableField<T,P> {
  @Override
  public T get(JsonObject json) {
    // if we have the nested key, read from that
    if (json.has(key)) {
      return loadable.convert(json.get(key), key);
    }
    // try reading from the current object, assumes the loadable supports JSON objects
    return loadable.convert(json, key);
  }

  /** Checks if the JSON has any conflicting keys */
  private boolean hasConflict(JsonObject parent, JsonObject serialized) {
    if (serialized.has(key)) {
      return true;
    }
    // check all the keys in the parent so far, if any of them exist then this conflicts
    for (String conflict : parent.keySet()) {
      if (serialized.has(conflict)) {
        return true;
      }
    }
    // check additional conflicts passed into the field, for the sake of optional fields mostly
    for (String conflict : conflicts) {
      if (serialized.has(conflict)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void serialize(P parent, JsonObject json) {
    JsonElement element = loadable.serialize(getter.apply(parent));
    if (element.isJsonObject()) {
      JsonObject serialized = element.getAsJsonObject();
      // if the serialized element contains the key, we cannot store it directly as that will confuse deserializing
      if (!hasConflict(json, serialized)) {
        for (Entry<String,JsonElement> entry : serialized.entrySet()) {
          json.add(entry.getKey(), entry.getValue());
        }
        return;
      }
    }
    json.add(key, element);
  }
}
