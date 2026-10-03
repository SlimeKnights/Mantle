package slimeknights.mantle.data.loadable.registry;

import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.Optional;

/**
 * Loadable for a registry using holders instead of values for a built-in registry.
 * @param registry     Registry instance.
 * @param streamCodec  Stream codec for the registry key.
 * @param <T>  Registry type.
 * @see RegistryLoadable
 * @see DatapackRegistryLoadable
 */
public record RegistryHolderLoadable<T>(Registry<T> registry, StreamCodec<RegistryFriendlyByteBuf,Holder<T>> streamCodec) implements HolderLoadable<T> {
  public RegistryHolderLoadable(Registry<T> registry) {
    this(registry, ByteBufCodecs.holderRegistry(registry.key()));
  }

  @Override
  public ResourceKey<? extends Registry<T>> key() {
    return registry.key();
  }

  @Override
  public Holder<T> fromKey(ResourceLocation name, String key, TypedMap context) {
    Optional<Reference<T>> holder = registry.getHolder(name);
    if (holder.isPresent()) {
      return holder.get();
    }
    throw new JsonSyntaxException("Unable to parse " + key + " as registry " + registry.key().location() + " does not contain ID " + name);
  }
}
