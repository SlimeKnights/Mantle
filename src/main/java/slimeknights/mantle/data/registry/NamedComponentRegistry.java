package slimeknights.mantle.data.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;

/**
 * Generic registry of a component named by a resource location. Supports any arbitrary object without making any changes to it.
 * @param <T> Type of the component being registered.
 */
public class NamedComponentRegistry<T> implements Loadable<T> {
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

  /** Gets the key associated with a value */
  public ResourceLocation getKey(T value) {
    ResourceLocation key = getOptionalKey(value);
    if (key == null) {
      throw new IllegalStateException(errorText + value);
    }
    return key;
  }


  /* Json */

  @Override
  public T convert(JsonElement element, String key) {
    ResourceLocation name = JsonHelper.convertToResourceLocation(element, key);
    T value = getValue(name);
    if (value == null) {
      throw new JsonSyntaxException(errorText + name + " at '" + key + '\'');
    }
    return value;
  }

  @Override
  public JsonElement serialize(T object) {
    return new JsonPrimitive(getKey(object).toString());
  }


  /* Network */

  /** Writes the value to the buffer */
  @Override
  public void toNetwork(T value, FriendlyByteBuf buffer) {
    buffer.writeResourceLocation(getKey(value));
  }

  /** Writes the value to the buffer */
  public void toNetworkOptional(@Nullable T value, FriendlyByteBuf buffer) {
    // if null, just write an empty string, that is not a valid resource location anyways and saves us a byte
    if (value != null) {
      buffer.writeUtf(getKey(value).toString());
    } else {
      buffer.writeUtf("");
    }
  }

  /** Reads the given value from the network by resource location */
  private T fromNetwork(ResourceLocation name) {
    T value = getValue(name);
    if (value == null) {
      throw new DecoderException(errorText + name);
    }
    return value;
  }

  /** Parse the value from JSON */
  @Override
  public T fromNetwork(FriendlyByteBuf buffer) {
    return fromNetwork(buffer.readResourceLocation());
  }

  /** Parse the value from JSON */
  @Nullable
  public T fromNetworkOptional(FriendlyByteBuf buffer) {
    // empty string is not a valid resource location, so its a nice value to use for null, saves us a byte
    String key = buffer.readUtf(Short.MAX_VALUE);
    if (key.isEmpty()) {
      return null;
    }
    return fromNetwork(new ResourceLocation(key));
  }
}
