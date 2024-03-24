package slimeknights.mantle.data.loadable.record;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Function5;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.field.RecordField;
import slimeknights.mantle.util.typed.TypedMap;

/** Record loadable with 5 fields */
record RecordLoadable5<A,B,C,D,E,R>(
  RecordField<A,? super R> fieldA,
  RecordField<B,? super R> fieldB,
  RecordField<C,? super R> fieldC,
  RecordField<D,? super R> fieldD,
  RecordField<E,? super R> fieldE,
  Function5<A,B,C,D,E,R> constructor
) implements RecordLoadable<R> {
  @Override
  public R deserialize(JsonObject json, TypedMap<Object> context) {
    return constructor.apply(
      fieldA.get(json, context),
      fieldB.get(json, context),
      fieldC.get(json, context),
      fieldD.get(json, context),
      fieldE.get(json, context)
    );
  }

  @Override
  public void serialize(R object, JsonObject json) {
    fieldA.serialize(object, json);
    fieldB.serialize(object, json);
    fieldC.serialize(object, json);
    fieldD.serialize(object, json);
    fieldE.serialize(object, json);
  }

  @Override
  public R fromNetwork(FriendlyByteBuf buffer, TypedMap<Object> context) {
    return constructor.apply(
      fieldA.fromNetwork(buffer, context),
      fieldB.fromNetwork(buffer, context),
      fieldC.fromNetwork(buffer, context),
      fieldD.fromNetwork(buffer, context),
      fieldE.fromNetwork(buffer, context)
    );
  }

  @Override
  public void toNetwork(R object, FriendlyByteBuf buffer) {
    fieldA.toNetwork(object, buffer);
    fieldB.toNetwork(object, buffer);
    fieldC.toNetwork(object, buffer);
    fieldD.toNetwork(object, buffer);
    fieldE.toNetwork(object, buffer);
  }
}
