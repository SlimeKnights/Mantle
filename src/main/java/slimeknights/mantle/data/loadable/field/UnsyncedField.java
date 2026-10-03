package slimeknights.mantle.data.loadable.field;

import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.util.typed.TypedMap;

/** Field wrapper that does not sync the value to client, instead using a client value */
public record UnsyncedField<T,P>(LoadableField<T,P> field, @Nullable T clientValue) implements LoadableField<T,P> {
  public UnsyncedField(LoadableField<T,P> field) {
    this(field, field instanceof DefaultingField<T,P> defaulting ? defaulting.defaultValue() : null);
  }

  @Override
  public String key() {
    return field.key();
  }

  @Nullable
  @Override
  public T get(JsonObject json, String key, TypedMap context) {
    return field.get(json, key, context);
  }

  @Override
  public void serialize(P parent, JsonObject json) {
    field.serialize(parent, json);
  }

  @Override
  public T decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return clientValue;
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, P parent) {}
}
