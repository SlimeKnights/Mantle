package slimeknights.mantle.data.registry;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.ResourceLocationLoadable;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Function;

/** Shared logic for registries that map a resource location to an object. */
public abstract class AbstractNamedComponentRegistry<T> implements ResourceLocationLoadable<T> {
  /** Name to make exceptions clearer */
  protected final String errorText;

  public AbstractNamedComponentRegistry(String errorText) {
    this.errorText = errorText + " ";
  }

  /** Gets a value or null if missing */
  @Nullable
  public abstract T getValue(ResourceLocation name);

  /** Gets all keys registered */
  public abstract Collection<ResourceLocation> getKeys();

  /** Gets all keys registered */
  public abstract Collection<T> getValues();


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


  /* Fields */

  @Override
  public <P> LoadableField<T,P> nullableField(String key, Function<P,T> getter) {
    return new NullableField<>(this, key, getter);
  }

  /** Custom implementation of nullable field using our networking optional logic */
  private record NullableField<T,P>(AbstractNamedComponentRegistry<T> registry, String key, Function<P,T> getter) implements LoadableField<T,P> {
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
