package slimeknights.mantle.data.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.loadable.field.AlwaysPresentLoadableField;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;

import java.util.Map.Entry;
import java.util.function.Function;

/** Direct field for a registry, will automatically handle mapping the type from the JSON object */
public record DirectRegistryField<T extends IHaveLoader<T>,P>(GenericLoaderRegistry<T> loadable, String typeKey, Function<P,T> getter) implements AlwaysPresentLoadableField<T,P> {
  /** Moves the passed type key to "type" */
  public static void mapType(JsonObject json, String typeKey) {
    json.addProperty("type", GsonHelper.getAsString(json, typeKey));
    json.remove(typeKey);
  }

  /**
   * Serializes the passed object into the passed JSON
   * @param json      JSON target for serializing
   * @param typeKey   Key to use for "type" in the serialized value
   * @param loader    Loader for serializing the value
   * @param value     Value to serialized
   * @param <N>  Type of value
   */
  public static <N extends IHaveLoader<N>> void serializeInto(JsonObject json, String typeKey, GenericLoaderRegistry<N> loader, N value) {
    JsonElement element = loader.serialize(value);
    // if its an object, copy all the data over
    if (element.isJsonObject()) {
      JsonObject nestedObject = element.getAsJsonObject();
      for (Entry<String, JsonElement> entry : nestedObject.entrySet()) {
        String key = entry.getKey();
        if (typeKey.equals(key)) {
          throw new JsonIOException("Unable to serialize nested object, object already has key " + typeKey);
        }
        if ("type".equals(key)) {
          key = typeKey;
        }
        json.add(key, entry.getValue());
      }
    } else if (element.isJsonPrimitive()){
      // if its a primitive, its the type ID, add just that by itself
      json.add(typeKey, element);
    } else {
      throw new JsonIOException("Unable to serialize nested object, expected string or object");
    }
  }

  @Override
  public T get(JsonObject json) throws JsonSyntaxException {
    // replace our type with the nested type, then run the nested loader
    mapType(json, typeKey);
    return loadable.deserialize(json);
  }

  @Override
  public void serialize(P parent, JsonObject json) {
    serializeInto(json, typeKey, loadable, getter.apply(parent));
  }
}
