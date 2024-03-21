package slimeknights.mantle.data.loadable.record;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Function6;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.field.LoadableField;

/** Record loadable with 6 fields */
@SuppressWarnings("DuplicatedCode")
record RecordLoadable6<A,B,C,D,E,F,G,R>(
  LoadableField<A,R> fieldA,
  LoadableField<B,R> fieldB,
  LoadableField<C,R> fieldC,
  LoadableField<D,R> fieldD,
  LoadableField<E,R> fieldE,
  LoadableField<F,R> fieldF,
  Function6<A,B,C,D,E,F,R> constructor
) implements RecordLoadable<R> {
  @Override
  public R deserialize(JsonObject json) {
    return constructor.apply(
      fieldA.get(json),
      fieldB.get(json),
      fieldC.get(json),
      fieldD.get(json),
      fieldE.get(json),
      fieldF.get(json)
    );
  }

  @Override
  public void serialize(R object, JsonObject json) {
    fieldA.serialize(object, json);
    fieldB.serialize(object, json);
    fieldC.serialize(object, json);
    fieldD.serialize(object, json);
    fieldE.serialize(object, json);
    fieldF.serialize(object, json);
  }

  @Override
  public R fromNetwork(FriendlyByteBuf buffer) throws DecoderException {
    return constructor.apply(
      fieldA.fromNetwork(buffer),
      fieldB.fromNetwork(buffer),
      fieldC.fromNetwork(buffer),
      fieldD.fromNetwork(buffer),
      fieldE.fromNetwork(buffer),
      fieldF.fromNetwork(buffer)
    );
  }

  @Override
  public void toNetwork(R object, FriendlyByteBuf buffer) throws EncoderException {
    fieldA.toNetwork(object, buffer);
    fieldB.toNetwork(object, buffer);
    fieldC.toNetwork(object, buffer);
    fieldD.toNetwork(object, buffer);
    fieldE.toNetwork(object, buffer);
    fieldF.toNetwork(object, buffer);
  }
}
