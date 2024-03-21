package slimeknights.mantle.data.loadable.record;

import com.google.gson.JsonObject;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.field.LoadableField;

import java.util.function.Function;

/** Record loadable with a single field */
record RecordLoadable1<A,R>(
  LoadableField<A,? super R> fieldA,
  Function<A,R> constructor
) implements RecordLoadable<R> {
  @Override
  public R deserialize(JsonObject json) {
    return constructor.apply(fieldA.get(json));
  }

  @Override
  public void serialize(R object, JsonObject json) {
    fieldA.serialize(object, json);
  }

  @Override
  public R fromNetwork(FriendlyByteBuf buffer) throws DecoderException {
    return constructor.apply(fieldA.fromNetwork(buffer));
  }

  @Override
  public void toNetwork(R object, FriendlyByteBuf buffer) throws EncoderException {
    fieldA.toNetwork(object, buffer);
  }
}
