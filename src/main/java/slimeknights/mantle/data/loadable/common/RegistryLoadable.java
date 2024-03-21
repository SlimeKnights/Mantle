package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.RegistryHelper;

import java.util.Objects;

/** Loadable for a registry entry */
public record RegistryLoadable<T>(Registry<T> registry, ResourceLocation registryId) implements Loadable<T> {
  public RegistryLoadable(ResourceKey<? extends Registry<T>> registryId) {
    this(Objects.requireNonNull(RegistryHelper.getRegistry(registryId), "Unknown registry " + registryId.location()), registryId.location());
  }

  @SuppressWarnings("unchecked")
  public RegistryLoadable(Registry<T> registry) {
    this(registry, ((Registry<Registry<?>>)Registry.REGISTRY).getKey(registry));
  }

  @Override
  public T convert(JsonElement element, String key) throws JsonSyntaxException {
    ResourceLocation name = JsonHelper.convertToResourceLocation(element, key);
    if (registry.containsKey(name)) {
      T value = registry.get(name);
      if (value != null) {
        return value;
      }
    }
    throw new JsonSyntaxException("Registry " + registryId + " does not contain ID " + name);
  }

  @Override
  public JsonElement serialize(T object) {
    ResourceLocation location = registry.getKey(object);
    if (location == null) {
      throw new RuntimeException("Registry " + registryId + " does not contain object " + object);
    }
    return new JsonPrimitive(location.toString());
  }

  @Override
  public T fromNetwork(FriendlyByteBuf buffer) {
    int id = buffer.readVarInt();
    T value = registry.byId(id);
    if (value == null) {
      throw new DecoderException("Registry " + registryId + " does not contain ID " + id);
    }
    return value;
  }

  @Override
  public void toNetwork(T object, FriendlyByteBuf buffer) {
    buffer.writeId(registry, object);
  }
}
