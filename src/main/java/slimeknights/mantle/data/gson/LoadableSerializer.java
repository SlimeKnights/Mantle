package slimeknights.mantle.data.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.SimpleJsonDeserializer;
import slimeknights.mantle.data.loadable.SimpleJsonSerializer;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.mantle.util.typed.TypedMapBuilder;

import java.lang.reflect.Type;
import java.util.function.Consumer;

/** Implementation of {@link com.google.gson.Gson} serializer and deserializers using {@link Loadable}. */
public record LoadableSerializer<T>(SimpleJsonDeserializer<? extends T> deserializer, SimpleJsonSerializer<? super T> serializer, Consumer<TypedMapBuilder> context) implements JsonSerializer<T>, JsonDeserializer<T> {
  public LoadableSerializer(Loadable<T> loadable, Consumer<TypedMapBuilder> context) {
    this(loadable, loadable, context);
  }

  /** Creates the context for this method */
  private TypedMap createContext() {
    TypedMapBuilder builder = TypedMapBuilder.builder();
    this.context.accept(builder);
    return builder.build();
  }

  @Override
  public T deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
    return deserializer.convert(json, type.getTypeName(), createContext());
  }

  @Override
  public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
    return serializer.serialize(src, createContext());
  }
}
