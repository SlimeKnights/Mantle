package slimeknights.mantle.data.loadable.record;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Function8;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.data.loadable.field.RecordField;
import slimeknights.mantle.util.typed.TypedMap;

/** Record loadable with 8 fields */
@SuppressWarnings("DuplicatedCode")
record RecordLoadable8<A,B,C,D,E,F,G,H,R>(
  RecordField<A,? super R> fieldA,
  RecordField<B,? super R> fieldB,
  RecordField<C,? super R> fieldC,
  RecordField<D,? super R> fieldD,
  RecordField<E,? super R> fieldE,
  RecordField<F,? super R> fieldF,
  RecordField<G,? super R> fieldG,
  RecordField<H,? super R> fieldH,
  Function8<A,B,C,D,E,F,G,H,R> constructor
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
      fieldH.get(json, context)
    );
  }

  @Override
  public void serializeInto(R object, JsonObject json, TypedMap context) {
    fieldA.serializeInto(object, json, context);
    fieldB.serializeInto(object, json, context);
    fieldC.serializeInto(object, json, context);
    fieldD.serializeInto(object, json, context);
    fieldE.serializeInto(object, json, context);
    fieldF.serializeInto(object, json, context);
    fieldG.serializeInto(object, json, context);
    fieldH.serializeInto(object, json, context);
  }

  @Override
  public R decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return constructor.apply(
      fieldA.decode(buffer, context),
      fieldB.decode(buffer, context),
      fieldC.decode(buffer, context),
      fieldD.decode(buffer, context),
      fieldE.decode(buffer, context),
      fieldF.decode(buffer, context),
      fieldG.decode(buffer, context),
      fieldH.decode(buffer, context)
    );
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, R object, TypedMap context) {
    fieldA.encode(buffer, object, context);
    fieldB.encode(buffer, object, context);
    fieldC.encode(buffer, object, context);
    fieldD.encode(buffer, object, context);
    fieldE.encode(buffer, object, context);
    fieldF.encode(buffer, object, context);
    fieldG.encode(buffer, object, context);
    fieldH.encode(buffer, object, context);
  }
}
