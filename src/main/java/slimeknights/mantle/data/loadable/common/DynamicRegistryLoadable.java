package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonSyntaxException;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.CommonHooks;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.primitive.ResourceLocationLoadable;
import slimeknights.mantle.util.typed.TypedMap;

/** Loadable for dynamic registries that are only available through a runtime lookup. */
public record DynamicRegistryLoadable<T>(ResourceKey<? extends Registry<T>> registryKey) implements ResourceLocationLoadable<T> {
  /** Gets the active lookup for this registry. */
  private HolderLookup.RegistryLookup<T> lookup(String key, TypedMap context) {
    HolderLookup.Provider provider = context.get(ContextKey.REGISTRY_ACCESS);
    HolderLookup.RegistryLookup<T> lookup = provider == null ? CommonHooks.resolveLookup(registryKey) : provider.lookup(registryKey).orElse(null);
    if (lookup == null) {
      throw new JsonSyntaxException("Unable to parse " + key + " as registry " + registryKey.location() + " cannot be located");
    }
    return lookup;
  }

  @Override
  public T fromKey(ResourceLocation name, String key, TypedMap context) {
    return lookup(key, context).get(ResourceKey.create(registryKey, name))
      .map(holder -> holder.value())
      .orElseThrow(() -> new JsonSyntaxException("Unable to parse " + key + " as registry " + registryKey.location() + " does not contain ID " + name));
  }

  @Override
  public ResourceLocation getKey(T object) {
    return lookup("value", TypedMap.EMPTY).listElements()
      .filter(holder -> holder.value() == object)
      .map(holder -> holder.key().location())
      .findFirst()
      .orElseThrow(() -> new EncoderException("Registry " + registryKey.location() + " does not contain object " + object));
  }

  @Override
  public T decode(FriendlyByteBuf buffer, TypedMap context) {
    ResourceLocation name = buffer.readResourceLocation();
    try {
      return fromKey(name, "packet", context);
    } catch (JsonSyntaxException e) {
      throw new DecoderException(e);
    }
  }

  @Override
  public void encode(FriendlyByteBuf buffer, T object) {
    buffer.writeResourceLocation(getKey(object));
  }
}
