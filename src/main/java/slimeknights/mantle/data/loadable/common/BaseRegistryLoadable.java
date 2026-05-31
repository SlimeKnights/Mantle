package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonSyntaxException;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.primitive.ResourceLocationLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;

/** Common logic for {@link RegistryLoadable} and {@link LazyRegistryLoadable} */
public interface BaseRegistryLoadable<T> extends ResourceLocationLoadable<T> {
  /** Gets the registry associated with this loadable. Null if the registry cannot be located */
  @Nullable
  Registry<T> registry();

  /** Gets the ID of this registry for error messages */
  ResourceLocation registryId();

  @Override
  default T fromKey(ResourceLocation name, String key, TypedMap context) {
    Registry<T> registry = registry();
    if (registry != null && registry.containsKey(name)) {
      T value = registry.get(name);
      if (value != null) {
        return value;
      }
    }
    throw new JsonSyntaxException("Unable to parse " + key + " as registry " + registryId() + " does not contain ID " + name);
  }

  @Override
  default ResourceLocation getKey(T object) {
    Registry<T> registry = registry();
    if (registry != null) {
      ResourceLocation location = registry.getKey(object);
      if (location != null) {
        return location;
      }
    }
    throw new RuntimeException("Registry " + registryId() + " does not contain object " + object);
  }

  @Override
  default T decode(FriendlyByteBuf buffer, TypedMap context) {
    int id = buffer.readVarInt();
    Registry<T> registry = registry();
    if (registry != null) {
      T value = registry.byId(id);
      if (value != null) {
        return value;
      }
    }
    throw new DecoderException("Registry " + registryId() + " does not contain ID " + id);
  }

  @Override
  default void encode(FriendlyByteBuf buffer, T object) {
    Registry<T> registry = registry();
    if (registry == null) {
      throw new EncoderException("Registry " + registryId() + " cannot be located");
    }
    buffer.writeVarInt(registry.getId(object));
  }
}

