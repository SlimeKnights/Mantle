package slimeknights.mantle.data.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.ResourceLocationLoadable;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Generic registry of a component named by a resource location. Supports any arbitrary object without making any changes to it.
 * @param <T> Type of the component being registered.
 */
public class NamedComponentRegistry<T> implements ResourceLocationLoadable<T> {
  /** Registered box expansion types */
  private final BiMap<ResourceLocation,T> values = HashBiMap.create();
  /** Name to make exceptions clearer */
  private final String errorText;

  public NamedComponentRegistry(String errorText) {
    this.errorText = errorText + " ";
  }

  /** Registers the value with the given name */
  public <V extends T> V register(ResourceLocation name, V value) {
    if (values.putIfAbsent(name, value) != null) {
      throw new IllegalArgumentException("Duplicate registration " + name);
    }
    return value;
  }

  /** Gets a value or null if missing */
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


  /* Json */

  @Override
  public T fromKey(ResourceLocation name, String key) {
    T value = getValue(name);
    if (value != null) {
      return value;
    }
    throw new JsonSyntaxException(errorText + name + " at '" + key + '\'');
  }


  /* Network */

  /** Writes the value to the buffer */
  @Override
  public void encode(FriendlyByteBuf buffer, T value) {
    buffer.writeResourceLocation(getKey(value));
  }

  /** Writes the value to the buffer */
  public void encodeOptional(FriendlyByteBuf buffer, @Nullable T value) {
    // if null, just write an empty string, that is not a valid resource location anyways and saves us a byte
    if (value != null) {
      buffer.writeUtf(getKey(value).toString());
    } else {
      buffer.writeUtf("");
    }
  }

  /** Reads the given value from the network by resource location */
  private T decodeInternal(ResourceLocation name) {
    T value = getValue(name);
    if (value == null) {
      throw new DecoderException(errorText + name);
    }
    return value;
  }

  /** Parse the value from JSON */
  @Override
  public T decode(FriendlyByteBuf buffer) {
    return decodeInternal(buffer.readResourceLocation());
  }

  /** Parse the value from JSON */
  @Nullable
  public T decodeOptional(FriendlyByteBuf buffer) {
    // empty string is not a valid resource location, so its a nice value to use for null, saves us a byte
    String key = buffer.readUtf(Short.MAX_VALUE);
    if (key.isEmpty()) {
      return null;
    }
    return decodeInternal(new ResourceLocation(key));
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


  /* Fields */

  @Override
  public <P> LoadableField<T,P> nullableField(String key, Function<P,T> getter) {
    return new NullableField<>(this, key, getter);
  }

  /** Custom implementation of nullable field using our networking optional logic */
  private record NullableField<T,P>(NamedComponentRegistry<T> registry, String key, Function<P,T> getter) implements LoadableField<T,P> {
    @Nullable
    @Override
    public T get(JsonObject json) {
      return registry.getOrDefault(json, key, null);
    }

    @Override
    public void serialize(P parent, JsonObject json) {
      T object = getter.apply(parent);
      if (object != null) {
        json.add(key, registry.serialize(object));
      }
    }

    @Nullable
    @Override
    public T decode(FriendlyByteBuf buffer) {
      return registry.decodeOptional(buffer);
    }

    @Override
    public void encode(FriendlyByteBuf buffer, P parent) {
      registry.encodeOptional(buffer, getter.apply(parent));
    }
  }
}
