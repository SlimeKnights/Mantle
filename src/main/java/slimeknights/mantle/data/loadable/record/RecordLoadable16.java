package slimeknights.mantle.data.loadable.record;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Function16;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.field.LoadableField;

/** Record loadable with 16 fields */
@SuppressWarnings("DuplicatedCode")
record RecordLoadable16<A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,R>(
  LoadableField<A,? super R> fieldA,
  LoadableField<B,? super R> fieldB,
  LoadableField<C,? super R> fieldC,
  LoadableField<D,? super R> fieldD,
  LoadableField<E,? super R> fieldE,
  LoadableField<F,? super R> fieldF,
  LoadableField<G,? super R> fieldG,
  LoadableField<H,? super R> fieldH,
  LoadableField<I,? super R> fieldI,
  LoadableField<J,? super R> fieldJ,
  LoadableField<K,? super R> fieldK,
  LoadableField<L,? super R> fieldL,
  LoadableField<M,? super R> fieldM,
  LoadableField<N,? super R> fieldN,
  LoadableField<O,? super R> fieldO,
  LoadableField<P,? super R> fieldP,
  Function16<A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,R> constructor
) implements RecordLoadable<R> {
  @Override
  public R deserialize(JsonObject json) {
    return constructor.apply(
      fieldA.get(json),
      fieldB.get(json),
      fieldC.get(json),
      fieldD.get(json),
      fieldE.get(json),
      fieldF.get(json),
      fieldG.get(json),
      fieldH.get(json),
      fieldI.get(json),
      fieldJ.get(json),
      fieldK.get(json),
      fieldL.get(json),
      fieldM.get(json),
      fieldN.get(json),
      fieldO.get(json),
      fieldP.get(json)
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
    fieldG.serialize(object, json);
    fieldH.serialize(object, json);
    fieldI.serialize(object, json);
    fieldJ.serialize(object, json);
    fieldK.serialize(object, json);
    fieldL.serialize(object, json);
    fieldM.serialize(object, json);
    fieldN.serialize(object, json);
    fieldO.serialize(object, json);
    fieldP.serialize(object, json);
  }

  @Override
  public R fromNetwork(FriendlyByteBuf buffer) throws DecoderException {
    return constructor.apply(
      fieldA.fromNetwork(buffer),
      fieldB.fromNetwork(buffer),
      fieldC.fromNetwork(buffer),
      fieldD.fromNetwork(buffer),
      fieldE.fromNetwork(buffer),
      fieldF.fromNetwork(buffer),
      fieldG.fromNetwork(buffer),
      fieldH.fromNetwork(buffer),
      fieldI.fromNetwork(buffer),
      fieldJ.fromNetwork(buffer),
      fieldK.fromNetwork(buffer),
      fieldL.fromNetwork(buffer),
      fieldM.fromNetwork(buffer),
      fieldN.fromNetwork(buffer),
      fieldO.fromNetwork(buffer),
      fieldP.fromNetwork(buffer)
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
    fieldG.toNetwork(object, buffer);
    fieldH.toNetwork(object, buffer);
    fieldI.toNetwork(object, buffer);
    fieldJ.toNetwork(object, buffer);
    fieldK.toNetwork(object, buffer);
    fieldL.toNetwork(object, buffer);
    fieldM.toNetwork(object, buffer);
    fieldN.toNetwork(object, buffer);
    fieldO.toNetwork(object, buffer);
    fieldP.toNetwork(object, buffer);
  }
}
