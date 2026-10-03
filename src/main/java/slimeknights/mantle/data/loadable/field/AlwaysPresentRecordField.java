package slimeknights.mantle.data.loadable.field;

import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.function.Function;

/** Common networking logic for fields that always have a network value */
public interface AlwaysPresentRecordField<T,P> extends RecordField<T,P> {
  /** Getter for the loadable */
  Loadable<T> loadable();
  /** Getter for the given field */
  Function<P,T> getter();

  @Override
  default T decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return loadable().decode(buffer, context);
  }

  @Override
  default void encode(RegistryFriendlyByteBuf buffer, P parent) {
    loadable().encode(buffer, getter().apply(parent));
  }
}
