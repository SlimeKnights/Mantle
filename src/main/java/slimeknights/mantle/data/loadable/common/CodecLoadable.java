package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.data.loadable.Loadable;

/** Implementation of a loadable using a codec. Note this will be inefficient when reading from and writing to the network */
public record CodecLoadable<T>(Codec<T> codec) implements Loadable<T> {
  @Override
  public T convert(JsonElement element, String key) throws JsonSyntaxException {
    return codec.parse(JsonOps.INSTANCE, element).getOrThrow(false, ErrorFactory.JSON_SYNTAX_ERROR);
  }

  @Override
  public JsonElement serialize(T object) throws RuntimeException {
    return codec.encodeStart(JsonOps.INSTANCE, object).getOrThrow(false, ErrorFactory.RUNTIME);
  }

  @Override
  public T fromNetwork(FriendlyByteBuf buffer) throws DecoderException {
    return buffer.readWithCodec(codec);
  }

  @Override
  public void toNetwork(T object, FriendlyByteBuf buffer) throws EncoderException {
    buffer.writeWithCodec(codec, object);
  }
}
