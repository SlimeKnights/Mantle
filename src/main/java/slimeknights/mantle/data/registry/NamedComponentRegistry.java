package slimeknights.mantle.data.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Generic registry of a component named by a resource location. Supports any arbitrary object without making any changes to it.
 * @param <T> Type of the component being registered.
 */
public class NamedComponentRegistry<T> extends AbstractNamedComponentRegistry<T> {
  /** Registered box expansion types */
  private final BiMap<ResourceLocation,T> values = HashBiMap.create();
  public NamedComponentRegistry(String errorText) {
    super(errorText);
  }

  /** Registers the value with the given name */
  public synchronized <V extends T> V register(ResourceLocation name, V value) {
    if (values.putIfAbsent(name, value) != null) {
      throw new IllegalArgumentException("Duplicate registration " + name);
    }
    return value;
  }

  @Override
  @Nullable
  public T getValue(ResourceLocation name) {
    return values.get(name);
  }

  /** Gets the key associated with a value */
  @Nullable
  public ResourceLocation getOptionalKey(T value) {
    return values.inverse().get(value);
  }

  @Override
  public ResourceLocation getKey(T value) {
    ResourceLocation key = getOptionalKey(value);
    if (key == null) {
      throw new IllegalStateException(errorText + value);
    }
    return key;
  }

  @Override
  public Collection<ResourceLocation> getKeys() {
    return values.keySet();
  }

  @Override
  public Collection<T> getValues() {
    return values.values();
  }


  /* Deprecated aliases */

  /** @deprecated use {@link #decode(FriendlyByteBuf)} */
  @Deprecated(forRemoval = true)
  public void toNetwork(T src, FriendlyByteBuf buffer) {
    encode(buffer, src);
  }

  /** @deprecated use {@link #decode(FriendlyByteBuf)} */
  @Deprecated(forRemoval = true)
  public T fromNetwork(FriendlyByteBuf buffer) {
    return decode(buffer);
  }

  /** @deprecated use {@link #decode(FriendlyByteBuf)} */
  @Deprecated(forRemoval = true)
  public void toNetworkOptional(@Nullable T src, FriendlyByteBuf buffer) {
    encodeOptional(buffer, src);
  }

  /** @deprecated use {@link #decode(FriendlyByteBuf)} */
  @Nullable
  @Deprecated(forRemoval = true)
  public T fromNetworkOptional(FriendlyByteBuf buffer) {
    return decodeOptional(buffer);
  }
}
