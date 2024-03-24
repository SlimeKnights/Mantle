package slimeknights.mantle.data.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.gson.GenericRegisteredSerializer;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.field.DefaultingField;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * Generic registry for an object that can both be sent over a friendly byte buffer and serialized into JSON.
 * @param <T>  Type of the serializable object
 * @see GenericRegisteredSerializer GenericRegisteredSerializer for an alternative that does not need to handle network syncing
 */
@SuppressWarnings("unused")  // API
public class GenericLoaderRegistry<T extends IHaveLoader> implements Loadable<T> {
  /** Empty object instance for compact deserialization */
  protected static final JsonObject EMPTY_OBJECT = new JsonObject();

  /** Display name for this registry */
  @Getter
  private final String name;
  /** Map of all serializers for implementations */
  protected final NamedComponentRegistry<IGenericLoader<? extends T>> loaders;
  /** Default instance, used for null values instead of null */
  @Nullable
  protected final T defaultInstance;
  /** If true, single key serializations will not use a JSON object to serialize, ideal for loaders with many singletons */
  protected final boolean compact;

  public GenericLoaderRegistry(String name, @Nullable T defaultInstance, boolean compact) {
    this.name = name;
    this.defaultInstance = defaultInstance;
    this.compact = compact;
    this.loaders = new NamedComponentRegistry<>("Unknown " + name + " loader");
  }

  public GenericLoaderRegistry(String name, boolean compact) {
    this(name, null, compact);
  }

  /** Registers a deserializer by name */
  public void register(ResourceLocation name, IGenericLoader<? extends T> loader) {
    loaders.register(name, loader);
  }

  @Override
  public T convert(JsonElement element, String key) throws JsonSyntaxException {
    if (defaultInstance != null && element.isJsonNull()) {
      return defaultInstance;
    }
    // first try object
    if (element.isJsonObject()) {
      JsonObject object = element.getAsJsonObject();
      return loaders.getIfPresent(object, "type").deserialize(object);
    }
    // try primitive if allowed
    if (compact && element.isJsonPrimitive()) {
      EMPTY_OBJECT.entrySet().clear();
      return loaders.convert(element, "type").deserialize(EMPTY_OBJECT);
    }
    // neither? failed to parse
    throw new JsonSyntaxException("Invalid " + name + " JSON at " + key + ", must be a JSON object" + (compact ? " or a string" : ""));
  }

  /**
   * Deserializes the object from JSON
   * @param element  JSON element
   * @return  Deserialized object
   */
  public T deserialize(JsonElement element) {
    return convert(element, "[unknown]");
  }

  /** Serializes the object to json, fighting generics */
  @SuppressWarnings("unchecked")
  private <L extends IHaveLoader> JsonElement serialize(IGenericLoader<L> loader, T src) {
    JsonObject json = new JsonObject();
    JsonElement type = new JsonPrimitive(loaders.getKey((IGenericLoader<? extends T>)loader).toString());
    json.add("type", type);
    loader.serialize((L)src, json);
    if (json.get("type") != type) {
      throw new IllegalStateException(name + " serializer " + type.getAsString() + " modified the type key, this is not allowed as it breaks deserialization");
    }
    // nothing to serialize? use type directly
    if (compact && json.entrySet().size() == 1) {
      return type;
    }
    return json;
  }

  @Override
  public JsonElement serialize(T src) {
    return serialize(src.getLoader(), src);
  }

  @Override
  public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
    if (src == defaultInstance) {
      return JsonNull.INSTANCE;
    }
    return serialize(src);
  }

  /** Writes the object to the network, fighting generics */
  @SuppressWarnings("unchecked")
  private <L extends IHaveLoader> void toNetwork(IGenericLoader<L> loader, T src, FriendlyByteBuf buffer) {
    loader.toNetwork((L)src, buffer);
  }

  @SuppressWarnings("unchecked")  // the cast is safe here as its just doing a map lookup, shouldn't cause harm if it fails. Besides, the loader has to extend T to work
  @Override
  public void toNetwork(T src, FriendlyByteBuf buffer) {
    // if we have a default instance, reading the loader is optional
    // if we match the default instance write no loader to save network space
    if (defaultInstance != null) {
      if (src == defaultInstance) {
        loaders.toNetworkOptional(null, buffer);
        return;
      }
      loaders.toNetworkOptional((IGenericLoader<? extends T>)src.getLoader(), buffer);
    } else {
      loaders.toNetwork((IGenericLoader<? extends T>)src.getLoader(), buffer);
    }
    toNetwork(src.getLoader(), src, buffer);
  }

  @Override
  public T fromNetwork(FriendlyByteBuf buffer) {
    IGenericLoader<? extends T> loader;
    // if we have a default instance, reading the loader is optional
    // if missing, use default instance
    if (defaultInstance != null) {
      loader = loaders.fromNetworkOptional(buffer);
      if (loader == null) {
        return defaultInstance;
      }
    } else {
      loader = loaders.fromNetwork(buffer);
    }
    return loader.fromNetwork(buffer);
  }

  /**
   * Creates a defaulting for this registry, using the internal default instance as the default
   * @param key               Json key
   * @param serializeDefault  If true, serializes the default instance. If false skips it
   * @param getter            Getter function
   * @param <P>  Field target
   * @return  Defaulting field instance
   */
  public <P> LoadableField<T,P> defaultField(String key, boolean serializeDefault, Function<P,T> getter) {
    if (defaultInstance != null) {
      return new DefaultingField<>(this, key, defaultInstance, serializeDefault, getter);
    }
    throw new IllegalStateException(name + " registry has no default instance, cannot make a default field");
  }

  /** Creates a defaulting field that does not serialize */
  public <P> LoadableField<T,P> defaultField(String key, Function<P,T> getter) {
    return defaultField(key, false, getter);
  }

  /** Creates a field that loads this object directly into the parent JSON object */
  public <P> LoadableField<T,P> directField(String typeKey, Function<P,T> getter) {
    return new DirectRegistryField<>(this, typeKey, getter);
  }

  @Override
  public String toString() {
    return getClass().getName() + "('" + name + "')";
  }

  /** Interface for a loader */
  public interface IGenericLoader<T> {
    /** Deserializes the object from json */
    T deserialize(JsonObject json);

    /** Reads the object from the packet buffer */
    T fromNetwork(FriendlyByteBuf buffer);

    /** Writes this object to json */
    void serialize(T object, JsonObject json);

    /** Writes this object to the packet buffer */
    void toNetwork(T object, FriendlyByteBuf buffer);
  }

  /** Interface for an object with a loader */
  public interface IHaveLoader {
    /** Gets the loader for the object */
    IGenericLoader<? extends IHaveLoader> getLoader();
  }

  /** Loader instance for an object with only a single implementation */
  @RequiredArgsConstructor
  public static class SingletonLoader<T> implements IGenericLoader<T> {
    @Getter
    private final T instance;

    /** Helper for creating a loader using an anonymous class */
    public SingletonLoader(Function<IGenericLoader<T>,T> creator) {
      this.instance = creator.apply(this);
    }

    @Override
    public T deserialize(JsonObject json) {
      return instance;
    }

    @Override
    public T fromNetwork(FriendlyByteBuf buffer) {
      return instance;
    }

    @Override
    public void serialize(T object, JsonObject json) {}

    @Override
    public void toNetwork(T object, FriendlyByteBuf buffer) {}

    /** Helper to create a singleton object as an anonymous class */
    public static <T> T singleton(Function<IGenericLoader<T>,T> instance) {
      return new SingletonLoader<>(instance).getInstance();
    }
  }
}
