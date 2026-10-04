package slimeknights.mantle.data.loadable.field;

import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.function.Function;

/**
 * Optional field that may be null
 * @param <P>  Parent object
 * @param <T>  Loadable type
 */
public record NullableField<T,P>(Loadable<T> loadable, String key, Function<P,T> getter) implements LoadableField<T,P> {
  @Override
  public T get(JsonObject json, String key, TypedMap context) {
    return loadable.getOrDefault(json, key, null, context);
  }

  @Override
  public void serializeInto(P parent, JsonObject json, TypedMap context) {
    T object = getter.apply(parent);
    if (object != null) {
      json.add(key, loadable.serialize(object, context));
    }
  }

  @Override
  public T decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    if (buffer.readBoolean()) {
      return loadable.decode(buffer, context);
    }
    return null;
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, P parent, TypedMap context) {
    T object = getter.apply(parent);
    if (object != null) {
      buffer.writeBoolean(true);
      loadable.encode(buffer, object, context);
    } else {
      buffer.writeBoolean(false);
    }
  }
}
