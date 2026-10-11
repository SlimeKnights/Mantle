package slimeknights.mantle.data.codec;

import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import lombok.RequiredArgsConstructor;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.mantle.util.typed.TypedMapBuilder;

import java.util.function.Consumer;
import java.util.stream.Stream;

/** Implementation of {@link com.mojang.serialization.MapCodec} using {@link RecordLoadable} */
@RequiredArgsConstructor
public class LoadableMapCodec<T> extends JsonMapCodec<T> {
  private final RecordLoadable<T> loadable;
  private final Consumer<TypedMapBuilder> context;

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
  public <O> Stream<O> keys(final DynamicOps<O> ops) {
    // TODO: add keys interface to record loadable and map using ops.createString?
    return Stream.of();
  }

  @Override
  protected T deserialize(JsonObject json, DynamicOps<?> ops) {
    return loadable.deserialize(json, createContext(ops));
  }

  @Override
  protected JsonObject serialize(T object, DynamicOps<?> ops) {
    JsonObject json = new JsonObject();
    loadable.serializeInto(object, json, createContext(ops));
    return json;
  }
}
