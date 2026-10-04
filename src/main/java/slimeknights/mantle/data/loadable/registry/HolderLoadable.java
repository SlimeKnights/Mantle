package slimeknights.mantle.data.loadable.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.primitive.ResourceLocationLoadable;
import slimeknights.mantle.util.typed.TypedMap;

/** Common logic between {@link RegistryHolderLoadable} and {@link DatapackRegistryLoadable} */
public interface HolderLoadable<T> extends ResourceLocationLoadable<Holder<T>> {
  /** Gets the key for this registry */
  ResourceKey<? extends Registry<T>> key();

  /** Gets the stream codec for this type. */
  StreamCodec<RegistryFriendlyByteBuf,Holder<T>> streamCodec();

  @Override
  default ResourceLocation getKey(Holder<T> holder, TypedMap context) {
    ResourceKey<T> key = holder.getKey();
    if (key != null) {
      return key.location();
    }
    throw new RuntimeException("Holder " + holder + " of registry " + key().location() + " has no key");
  }

  @Override
  default Holder<T> decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return streamCodec().decode(buffer);
  }

  @Override
  default void encode(RegistryFriendlyByteBuf buffer, Holder<T> holder, TypedMap context) {
    streamCodec().encode(buffer, holder);
  }
}
