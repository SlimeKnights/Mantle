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
  default T fromNetwork(FriendlyByteBuf buffer) {
    return loadable().fromNetwork(buffer);
  }

  @Override
  default void toNetwork(P parent, FriendlyByteBuf buffer) {
    loadable().toNetwork(getter().apply(parent), buffer);
  }
}
