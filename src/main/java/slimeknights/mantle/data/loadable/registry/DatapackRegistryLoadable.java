package slimeknights.mantle.data.loadable.registry;

import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.Optional;

/**
 * Loadable for a datapack registry.
 * Requires {@link slimeknights.mantle.data.loadable.field.ContextKey#REGISTRY_LOOKUP} to be set in the JSON parsing context.
 * For datagen, create holders using {@link HolderLookup#getOrThrow(ResourceKey)}. If registry access is not provided then using this loadable is likely not possible.
 * @param key          Registry key
 * @param streamCodec  Stream codec for the registry key.
 * @param <T>  Registry type.
 * @see RegistryHolderLoadable
 */
public record DatapackRegistryLoadable<T>(ResourceKey<? extends Registry<T>> key, StreamCodec<RegistryFriendlyByteBuf, Holder<T>> streamCodec) implements HolderLoadable<T> {
  public DatapackRegistryLoadable(ResourceKey<? extends Registry<T>> key) {
    this(key, ByteBufCodecs.holderRegistry(key));
  }

  @Override
  public Holder<T> fromKey(ResourceLocation name, String key, TypedMap context) {
    HolderLookup.Provider lookup = context.getOrThrow(ContextKey.REGISTRY_LOOKUP, ErrorFactory.JSON_SYNTAX_ERROR);
    Optional<Reference<T>> holder = lookup.lookupOrThrow(this.key).get(ResourceKey.create(this.key, name));
    if (holder.isPresent()) {
      return holder.get();
    }
    throw new JsonSyntaxException("Unable to parse " + key + " as registry " + this.key.location() + " does not contain ID " + name);
  }
}
