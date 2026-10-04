package slimeknights.mantle.data.loadable.field;

import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.util.typed.TypedMap;

/** Field wrapper that does not sync the value to client, instead using a client value */
public record UnsyncedField<T,P>(LoadableField<T,P> base, @Nullable T clientValue) implements LoadableFieldWrapper<T,P> {
  public UnsyncedField(LoadableField<T,P> field) {
    this(field, field instanceof DefaultingField<T,P> defaulting ? defaulting.defaultValue() : null);
  }

  @Override
  public T decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return clientValue;
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, P parent, TypedMap context) {}
}
