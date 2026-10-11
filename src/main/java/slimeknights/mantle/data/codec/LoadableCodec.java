package slimeknights.mantle.data.codec;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.RegistryOps;
import net.neoforged.neoforge.common.conditions.ICondition.IContext;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.SimpleJsonDeserializer;
import slimeknights.mantle.data.loadable.SimpleJsonSerializer;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.mantle.util.typed.TypedMapBuilder;

import java.util.function.Consumer;

/**
 * Implementation of a codec using a loadable.
 * Note this will be inefficient comparatively when using dynamic ops other than {@link com.mojang.serialization.JsonOps}.
 * Consider just writing a codec directly if that is needed.
 * @see slimeknights.mantle.data.loadable.common.CodecLoadable
 */
public record LoadableCodec<T>(SimpleJsonDeserializer<? extends T> deserializer, SimpleJsonSerializer<? super T> serializer, Consumer<TypedMapBuilder> context) implements JsonCodec<T> {
  public LoadableCodec(Loadable<T> loadable, Consumer<TypedMapBuilder> context) {
    this(loadable, loadable, context);
  }

  public LoadableCodec(Loadable<T> loadable) {
    this(loadable, TypedMapBuilder.EMPTY_CONSUMER);
  }

  /** Shared logic with {@link LoadableMapCodec} to prepare context from ops */
  public static <O> TypedMapBuilder prepareContext(DynamicOps<O> ops) {
    TypedMapBuilder builder = TypedMap.builder();
    if (ops instanceof RegistryOps<?> registryOps) {
      builder.put(ContextKey.REGISTRY_LOOKUP, registryOps.lookupProvider);
      builder.put(ContextKey.CONDITION_CONTEXT, MantleCodecs.CONDITION_CONTEXT.parse(ops, ops.empty()).result().orElse(IContext.EMPTY));
    }
    return builder;
  }

  /** Creates the context for this method */
  private TypedMap createContext(DynamicOps<?> ops) {
    TypedMapBuilder builder = prepareContext(ops);
    this.context.accept(builder);
    return builder.build();
  }

  @Override
  public T deserialize(JsonElement element, DynamicOps<?> ops) {
    return deserializer.convert(element, "codec", createContext(ops));
  }

  @Override
  public JsonElement serialize(T object, DynamicOps<?> ops) {
    return serializer.serialize(object, createContext(ops));
  }
}
