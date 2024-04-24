package slimeknights.mantle.data.loadable.record;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Function10;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.field.RecordField;
import slimeknights.mantle.util.typed.TypedMap;

/** Record loadable with 10 fields */
@SuppressWarnings("DuplicatedCode")
record RecordLoadable10<A,B,C,D,E,F,G,H,I,J,R>(
  RecordField<A,? super R> fieldA,
  RecordField<B,? super R> fieldB,
  RecordField<C,? super R> fieldC,
  RecordField<D,? super R> fieldD,
  RecordField<E,? super R> fieldE,
  RecordField<F,? super R> fieldF,
  RecordField<G,? super R> fieldG,
  RecordField<H,? super R> fieldH,
  RecordField<I,? super R> fieldI,
  RecordField<J,? super R> fieldJ,
  Function10<A,B,C,D,E,F,G,H,I,J,R> constructor
) implements RecordLoadable<R> {
  @Override
  public R deserialize(JsonObject json, TypedMap context) {
    return constructor.apply(
      fieldA.get(json, context),
      fieldB.get(json, context),
      fieldC.get(json, context),
      fieldD.get(json, context),
      fieldE.get(json, context),
      fieldF.get(json, context),
      fieldG.get(json, context),
      fieldH.get(json, context),
      fieldI.get(json, context),
      fieldJ.get(json, context)
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
  }

  @Override
  public R decode(FriendlyByteBuf buffer, TypedMap context) {
    return constructor.apply(
      fieldA.decode(buffer, context),
      fieldB.decode(buffer, context),
      fieldC.decode(buffer, context),
      fieldD.decode(buffer, context),
      fieldE.decode(buffer, context),
      fieldF.decode(buffer, context),
      fieldG.decode(buffer, context),
      fieldH.decode(buffer, context),
      fieldI.decode(buffer, context),
      fieldJ.decode(buffer, context)
    );
  }

  @Override
  public void encode(FriendlyByteBuf buffer, R object) {
    fieldA.encode(buffer, object);
    fieldB.encode(buffer, object);
    fieldC.encode(buffer, object);
    fieldD.encode(buffer, object);
    fieldE.encode(buffer, object);
    fieldF.encode(buffer, object);
    fieldG.encode(buffer, object);
    fieldH.encode(buffer, object);
    fieldI.encode(buffer, object);
    fieldJ.encode(buffer, object);
  }
}
