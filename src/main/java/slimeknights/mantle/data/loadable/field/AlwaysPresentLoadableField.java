package slimeknights.mantle.data.loadable.field;

import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.Loadable;

import java.util.function.Function;

/** Common networking logic for loadables that always have a network value */
public interface AlwaysPresentLoadableField<T,P> extends LoadableField<T,P> {
  /** Getter for the loadable */
  Loadable<T> loadable();
  /** Getter for the given field */
  Function<P,T> getter();

  @Override
  default T decode(FriendlyByteBuf buffer) {
    return loadable().decode(buffer);
  }

  @Override
  default void encode(FriendlyByteBuf buffer, P parent) {
    loadable().encode(buffer, getter().apply(parent));
  }
}
