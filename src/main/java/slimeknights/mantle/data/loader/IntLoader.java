package slimeknights.mantle.data.loader;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;

import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

/** @deprecated use {@link slimeknights.mantle.data.loadable.record.RecordLoadable} with {@link slimeknights.mantle.data.loadable.primitive.IntLoadable} */
@Deprecated
@RequiredArgsConstructor
public class IntLoader<T> implements IGenericLoader<T>, RecordLoadable<T> {
  private final String key;
  private final IntFunction<T> constructor;
  private final ToIntFunction<T> getter;

  @Override
  public void serialize(T object, JsonObject json) {
    json.addProperty(key, getter.applyAsInt(object));
  }

  @Override
  public T deserialize(JsonObject json) {
    return constructor.apply(GsonHelper.getAsInt(json, key));
  }

  @Override
  public void toNetwork(T object, FriendlyByteBuf buffer) {
    buffer.writeVarInt(getter.applyAsInt(object));
  }

  @Override
  public T fromNetwork(FriendlyByteBuf buffer) {
    return constructor.apply(buffer.readVarInt());
  }
}
