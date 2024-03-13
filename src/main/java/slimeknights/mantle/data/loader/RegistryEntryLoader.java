package slimeknights.mantle.data.loader;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.IForgeRegistry;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;
import slimeknights.mantle.util.JsonHelper;

import java.util.Objects;
import java.util.function.Function;

/**
 * Serializer for an object with a registry entry parameter
 * @param <O>  Object type
 * @param <V>  Registry entry type
 * @see RegistrySetLoader
 */
public record RegistryEntryLoader<O extends IHaveLoader<?>,V>(
  String key,
  IForgeRegistry<V> registry,
  Function<V,O> constructor,
  Function<O,V> getter
) implements IGenericLoader<O> {

  @Override
  public O deserialize(JsonObject json) {
    return constructor.apply(JsonHelper.getAsEntry(registry, json, key));
  }

  @Override
  public void serialize(O object, JsonObject json) {
    json.addProperty(key, Objects.requireNonNull(registry.getKey(getter.apply(object))).toString());
  }

  @Override
  public O fromNetwork(FriendlyByteBuf buffer) {
    return constructor.apply(buffer.readRegistryIdUnsafe(registry));
  }

  @Override
  public void toNetwork(O object, FriendlyByteBuf buffer) {
    buffer.writeRegistryIdUnsafe(registry, getter.apply(object));
  }
}
