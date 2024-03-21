package slimeknights.mantle.data.loader;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.util.JsonHelper;

import java.util.function.Function;

/**
 * Loader for an object with a resource location
 * @param <O>  Object type
 * @deprecated use {@link RecordLoadable} and {@link slimeknights.mantle.data.loadable.Loadables#RESOURCE_LOCATION}
 */
@Deprecated
public record ResourceLocationLoader<O>(
  String key,
  Function<ResourceLocation,O> constructor,
  Function<O,ResourceLocation> getter
) implements IGenericLoader<O>, RecordLoadable<O> {
  @Override
  public O deserialize(JsonObject json) {
    return constructor.apply(JsonHelper.getResourceLocation(json, key));
  }

  @Override
  public O fromNetwork(FriendlyByteBuf buffer) {
    return constructor.apply(buffer.readResourceLocation());
  }

  @Override
  public void serialize(O object, JsonObject json) {
    json.addProperty(key, getter.apply(object).toString());
  }

  @Override
  public void toNetwork(O object, FriendlyByteBuf buffer) {
    buffer.writeResourceLocation(getter.apply(object));
  }
}
