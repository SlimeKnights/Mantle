package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.data.loadable.Loadable;

/** Implementation of a loadable using a codec. Note this will be inefficient when reading from and writing to the network */
public record CodecLoadable<T>(Codec<T> codec) implements Loadable<T> {
  @Override
  public T convert(JsonElement element, String key) {
    return codec.parse(JsonOps.INSTANCE, element).getOrThrow(false, ErrorFactory.JSON_SYNTAX_ERROR);
  }

  @Override
  public JsonElement serialize(T object) {
    return codec.encodeStart(JsonOps.INSTANCE, object).getOrThrow(false, ErrorFactory.RUNTIME);
  }

  @Override
  public T decode(FriendlyByteBuf buffer) {
    return buffer.readWithCodec(codec);
  }

  @Override
  public void encode(FriendlyByteBuf buffer, T object) {
    buffer.writeWithCodec(codec, object);
  }
}
