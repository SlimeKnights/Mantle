package slimeknights.mantle.data.loader;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.DirectRegistryField;
import slimeknights.mantle.data.registry.GenericLoaderRegistry;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;

import java.util.function.Function;

/**
 * Loader that loads from another loader
 * @param <T>  Object being loaded
 * @param <N>  Nested object type
 * @deprecated use {@link RecordLoadable} with {@link GenericLoaderRegistry#directField(String, Function)}
 */
@Deprecated
public record NestedLoader<T,N extends IHaveLoader>(
  String typeKey,
  GenericLoaderRegistry<N> nestedLoader,
  Function<N, T> constructor,
  Function<T, N> getter
) implements IGenericLoader<T>, RecordLoadable<T> {
  /** @deprecated use {@link DirectRegistryField#mapType(JsonObject, String)} */
  @Deprecated
  public static void mapType(JsonObject json, String typeKey) {
    DirectRegistryField.mapType(json, typeKey);
  }

  @Override
  public T deserialize(JsonObject json) {
    mapType(json, typeKey);
    return constructor.apply(nestedLoader.deserialize(json));
  }

  /** @deprecated use {@link DirectRegistryField#serializeInto(JsonObject, String, GenericLoaderRegistry, IHaveLoader)} */
  @Deprecated
  public static <N extends IHaveLoader> void serializeInto(JsonObject json, String typeKey, GenericLoaderRegistry<N> loader, N value) {
    DirectRegistryField.serializeInto(json, typeKey, loader, value);
  }

  @Override
  public void serialize(T object, JsonObject json) {
    serializeInto(json, typeKey, nestedLoader, getter.apply(object));
  }

  @Override
  public T fromNetwork(FriendlyByteBuf buffer) {
    return constructor.apply(nestedLoader.fromNetwork(buffer));
  }

  @Override
  public void toNetwork(T object, FriendlyByteBuf buffer) {
    nestedLoader.toNetwork(getter.apply(object), buffer);
  }
}
