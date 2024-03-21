package slimeknights.mantle.data.loadable.record;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Function3;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.field.LoadableField;

/** Record loadable with 3 fields */
record RecordLoadable3<A,B,C,R>(
  LoadableField<A,R> fieldA,
  LoadableField<B,R> fieldB,
  LoadableField<C,R> fieldC,
  Function3<A,B,C,R> constructor
) implements RecordLoadable<R> {
  @Override
  public R deserialize(JsonObject json) {
    return constructor.apply(
      fieldA.get(json),
      fieldB.get(json),
      fieldC.get(json)
    );
  }

  @Override
  public void serialize(R object, JsonObject json) {
    fieldA.serialize(object, json);
    fieldB.serialize(object, json);
    fieldC.serialize(object, json);
  }

  @Override
  public R fromNetwork(FriendlyByteBuf buffer) throws DecoderException {
    return constructor.apply(
      fieldA.fromNetwork(buffer),
      fieldB.fromNetwork(buffer),
      fieldC.fromNetwork(buffer)
    );
  }

  @Override
  public void toNetwork(R object, FriendlyByteBuf buffer) throws EncoderException {
    fieldA.toNetwork(object, buffer);
    fieldB.toNetwork(object, buffer);
    fieldC.toNetwork(object, buffer);
  }
}
