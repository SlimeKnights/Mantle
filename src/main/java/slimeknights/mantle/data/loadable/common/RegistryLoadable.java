package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonSyntaxException;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.primitive.ResourceLocationLoadable;
import slimeknights.mantle.util.RegistryHelper;

import java.util.Objects;

/** Loadable for a registry entry */
public record RegistryLoadable<T>(Registry<T> registry, ResourceLocation registryId) implements ResourceLocationLoadable<T> {
  public RegistryLoadable(ResourceKey<? extends Registry<T>> registryId) {
    this(Objects.requireNonNull(RegistryHelper.getRegistry(registryId), "Unknown registry " + registryId.location()), registryId.location());
  }

  @SuppressWarnings("unchecked")
  public RegistryLoadable(Registry<T> registry) {
    this(registry, ((Registry<Registry<?>>)Registry.REGISTRY).getKey(registry));
  }

  @Override
  public T fromKey(ResourceLocation name, String key) {
    if (registry.containsKey(name)) {
      T value = registry.get(name);
      if (value != null) {
        return value;
      }
    }
    throw new JsonSyntaxException("Unable to parse " + key + " as registry " + registryId + " does not contain ID " + name);
  }

  @Override
  public ResourceLocation getKey(T object) {
    ResourceLocation location = registry.getKey(object);
    if (location == null) {
      throw new RuntimeException("Registry " + registryId + " does not contain object " + object);
    }
    return location;
  }

  @Override
  public T decode(FriendlyByteBuf buffer) {
    int id = buffer.readVarInt();
    T value = registry.byId(id);
    if (value == null) {
      throw new DecoderException("Registry " + registryId + " does not contain ID " + id);
    }
    return value;
  }

  @Override
  public void encode(FriendlyByteBuf buffer, T object) {
    buffer.writeId(registry, object);
  }
}
