package slimeknights.mantle.data.loadable.field;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import slimeknights.mantle.data.loadable.Loadable;

import java.util.function.Function;

/**
 * A basic required field with a name
 * @param serializeNull  If true, null JsonElements are serialized, if false null is treated as don't serialize (for defaults)
 * @param <P>  Parent object
 * @param <T>  Loadable type
 */
public record NamedField<T,P>(Loadable<T> loadable, String key, boolean serializeNull, Function<P,T> getter) implements AlwaysPresentLoadableField<T,P> {
  @Override
  public T get(JsonObject json) throws JsonSyntaxException {
    return loadable.getAndDeserialize(json, key);
  }

  @Override
  public void serialize(P parent, JsonObject json) {
    JsonElement element = loadable.serialize(getter.apply(parent));
    if (serializeNull || !element.isJsonNull()) {
      json.add(key, element);
    }
  }
}
