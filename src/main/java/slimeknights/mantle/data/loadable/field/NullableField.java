package slimeknights.mantle.data.loadable.field;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.Loadable;

import java.util.function.Function;

/**
 * Optional field that may be null
 * @param <P>  Parent object
 * @param <T>  Loadable type
 */
public record NullableField<T,P>(Loadable<T> loadable, String key, Function<P,T> getter) implements LoadableField<T,P> {
  @Override
  public T get(JsonObject json) {
    return loadable.getOrDefault(json, key, null);
  }

  @Override
  public void serialize(P parent, JsonObject json) {
    T object = getter.apply(parent);
    if (object != null) {
      json.add(key, loadable.serialize(object));
    }
  }

  @Override
  public T fromNetwork(FriendlyByteBuf buffer) {
    if (buffer.readBoolean()) {
      return loadable.fromNetwork(buffer);
    }
    return null;
  }

  @Override
  public void toNetwork(P parent, FriendlyByteBuf buffer) {
    T object = getter.apply(parent);
    if (object != null) {
      buffer.writeBoolean(true);
      loadable.toNetwork(object, buffer);
    } else {
      buffer.writeBoolean(false);
    }
  }
}
