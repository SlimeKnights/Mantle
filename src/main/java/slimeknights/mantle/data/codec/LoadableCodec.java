package slimeknights.mantle.data.codec;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.RegistryOps;
import net.neoforged.neoforge.common.conditions.ICondition.IContext;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.mantle.util.typed.TypedMapBuilder;

import java.util.function.Consumer;

/** Implementation of a codec using a loadable. Note this will be inefficient comparatively when using {@link net.minecraft.nbt.NbtOps} */
public record LoadableCodec<T>(Loadable<T> loadable, Consumer<TypedMapBuilder> context) implements JsonCodec<T> {
  public LoadableCodec(Loadable<T> loadable) {
    this(loadable, TypedMapBuilder.EMPTY_CONSUMER);
  }

  /** Creates the context for this method */
  private <O> TypedMap createContext(DynamicOps<O> ops) {
    TypedMapBuilder builder = TypedMap.builder();
    if (ops instanceof RegistryOps<?> registryOps && registryOps.lookupProvider instanceof RegistryOps.HolderLookupAdapter adapter) {
      builder.put(ContextKey.REGISTRY_LOOKUP, adapter.lookupProvider);
      // grab the condition context, or empty context if its missing
      builder.put(ContextKey.CONDITION_CONTEXT, MantleCodecs.CONDITION_CONTEXT.parse(ops, ops.empty()).result().orElse(IContext.EMPTY));
    }
    this.context.accept(builder);
    return builder.build();
  }

  @Override
  public T deserialize(JsonElement element, DynamicOps<?> ops) {
    return loadable.convert(element, "codec", createContext(ops));
  }

  @Override
  public JsonElement serialize(T object, DynamicOps<?> ops) {
    return loadable.serialize(object, createContext(ops));
  }
}
