package slimeknights.mantle.data.codec;

import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import lombok.RequiredArgsConstructor;
import slimeknights.mantle.data.loadable.record.RecordJsonDeserializer;
import slimeknights.mantle.data.loadable.record.RecordJsonSerializer;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.mantle.util.typed.TypedMapBuilder;

import java.util.function.Consumer;

/**
 * Implementation of {@link com.mojang.serialization.MapCodec} using {@link RecordLoadable}.
 * @see LoadableCodec
 */
@RequiredArgsConstructor
public class LoadableMapCodec<T> extends JsonMapCodec<T> {
  private final RecordJsonDeserializer<? extends T> deserializer;
  private final RecordJsonSerializer<? super T> serializer;
  private final Consumer<TypedMapBuilder> context;

  public LoadableMapCodec(RecordLoadable<T> loadable, Consumer<TypedMapBuilder> context) {
    this(loadable, loadable, context);
  }

  public LoadableMapCodec(RecordLoadable<T> loadable) {
    this(loadable, TypedMapBuilder.EMPTY_CONSUMER);
  }

  /** Creates the context for this method */
  private TypedMap createContext(DynamicOps<?> ops) {
    TypedMapBuilder builder = LoadableCodec.prepareContext(ops);
    this.context.accept(builder);
    return builder.build();
  }

  @Override
  protected T deserialize(JsonObject json, DynamicOps<?> ops) {
    return deserializer.deserialize(json, createContext(ops));
  }

  @Override
  protected JsonObject serialize(T object, DynamicOps<?> ops) {
    JsonObject json = new JsonObject();
    serializer.serializeInto(object, json, createContext(ops));
    return json;
  }
}
