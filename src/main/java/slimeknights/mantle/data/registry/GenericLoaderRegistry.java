package slimeknights.mantle.data.registry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.gson.GenericRegisteredSerializer;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * Generic registry for an object that can both be sent over a friendly byte buffer and serialized into JSON.
 * @param <T>  Type of the serializable object
 * @see GenericRegisteredSerializer GenericRegisteredSerializer for an alternative that does not need to handle network syncing
 */
@RequiredArgsConstructor
public class GenericLoaderRegistry<T extends IHaveLoader<T>> implements JsonSerializer<T>, JsonDeserializer<T> {
  /** Empty object instance for compact deserialization */
  private static final JsonObject EMPTY_OBJECT = new JsonObject();
  /** Map of all serializers for implementations */
  private final NamedComponentRegistry<IGenericLoader<? extends T>> loaders = new NamedComponentRegistry<>("Unknown loader");


  /** Default instance, used for null values instead of null */
  @Nullable
  private final T defaultInstance;
  /** If true, single key serializations will not use a JSON object to serialize, ideal for loaders with many singletons */
  private final boolean compact;

  public GenericLoaderRegistry(T defaultInstance) {
    this(defaultInstance, false);
  }

  public GenericLoaderRegistry(boolean compact) {
    this(null, compact);
  }

  public GenericLoaderRegistry() {
    this(null, false);
  }

  /** Registers a deserializer by name */
  public void register(ResourceLocation name, IGenericLoader<? extends T> loader) {
    loaders.register(name, loader);
  }

  /**
   * Deserializes the object from JSON
   * @param element  JSON element
   * @return  Deserialized object
   */
  public T deserialize(JsonElement element) {
    if (defaultInstance != null && element.isJsonNull()) {
      return defaultInstance;
    }
    if (element.isJsonObject()) {
      JsonObject object = element.getAsJsonObject();
      return loaders.deserialize(object, "type").deserialize(object);
    }
    if (compact) {
      if (element.isJsonPrimitive()) {
        EMPTY_OBJECT.entrySet().clear();
        return loaders.convert(element, "type").deserialize(EMPTY_OBJECT);
      }
      throw new JsonSyntaxException("Invalid JSON for " + getClass().getSimpleName() + ", must be a JSON object or a string");
    } else {
      throw new JsonSyntaxException("Invalid JSON for " + getClass().getSimpleName() + ", must be a JSON object");
    }
  }

  /**
   * Deserializes the object from JSON
   * @param parent  JSON object parent
   * @param key     Key in the parent
   * @return  Deserialized object
   */
  public T getAndDeserialize(JsonObject parent, String key) {
    if (defaultInstance != null && !parent.has(key)) {
      return defaultInstance;
    }
    if (compact) {
      return deserialize(JsonHelper.getElement(parent, key));
    }
    return deserialize(GsonHelper.getAsJsonObject(parent, key));
  }

  @Override
  public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    return deserialize(json);
  }

  /** Serializes the object to json, fighting generics */
  @SuppressWarnings("unchecked")
  private <L extends IHaveLoader<T>> JsonElement serialize(IGenericLoader<L> loader, T src) {
    JsonObject json = new JsonObject();
    JsonElement type = new JsonPrimitive(loaders.getKey((IGenericLoader<? extends T>)loader).toString());
    json.add("type", type);
    loader.serialize((L)src, json);
    if (json.get("type") != type) {
      throw new IllegalStateException("Serializer " + type.getAsString() + " modified the type key, this is not allowed as it breaks deserialization");
    }
    // nothing to serialize? use type directly
    if (compact && json.entrySet().size() == 1) {
      return type;
    }
    return json;
  }

  /** Serializes the object to JSON */
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
  private <L extends IHaveLoader<T>> void toNetwork(IGenericLoader<L> loader, T src, FriendlyByteBuf buffer) {
    loader.toNetwork((L)src, buffer);
  }

  /** Writes the object to the network */
  public void toNetwork(T src, FriendlyByteBuf buffer) {
    // if we have a default instance, reading the loader is optional
    // if we match the default instance write no loader to save network space
    if (defaultInstance != null) {
      if (src == defaultInstance) {
        loaders.toNetworkOptional(null, buffer);
        return;
      }
      loaders.toNetworkOptional(src.getLoader(), buffer);
    } else {
      loaders.toNetwork(src.getLoader(), buffer);
    }
    toNetwork(src.getLoader(), src, buffer);
  }

  /**
   * Reads the object from the buffer
   * @param buffer  Buffer instance
   * @return  Read object
   */
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

  /** Interface for a loader */
  public interface IGenericLoader<T extends IHaveLoader<?>> {
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
  public interface IHaveLoader<T> {
    /** Gets the loader for the object */
    IGenericLoader<? extends T> getLoader();
  }

  /** Loader instance for an object with only a single implementation */
  @RequiredArgsConstructor
  public static class SingletonLoader<T extends IHaveLoader<?>> implements IGenericLoader<T> {
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
    public static <T extends IHaveLoader<?>> T singleton(Function<IGenericLoader<T>,T> instance) {
      return new SingletonLoader<>(instance).getInstance();
    }
  }
}
