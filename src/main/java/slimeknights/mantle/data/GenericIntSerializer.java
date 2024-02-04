package slimeknights.mantle.data;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.data.GenericLoaderRegistry.IHaveLoader;

import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

/**
 * Generic serializer for classes that just have a single int value.
 * TODO 1.19: move to {@link slimeknights.mantle.data.loader} as {@code IntLoader}
 */
@RequiredArgsConstructor
public class GenericIntSerializer<T extends IHaveLoader<?>> implements IGenericLoader<T> {
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
