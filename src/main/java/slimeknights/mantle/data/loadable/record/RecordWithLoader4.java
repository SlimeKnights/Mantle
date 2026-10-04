package slimeknights.mantle.data.loadable.record;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Function5;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.data.loadable.field.RecordField;
import slimeknights.mantle.util.typed.TypedMap;

/** Record loadable with 4 fields plus the loader itself */
record RecordWithLoader4<A,B,C,D,R>(
  RecordField<A,? super R> fieldA,
  RecordField<B,? super R> fieldB,
  RecordField<C,? super R> fieldC,
  RecordField<D,? super R> fieldD,
  Function5<A,B,C,D,RecordLoadable<R>,R> constructor
) implements RecordLoadable<R> {
  @Override
  public R deserialize(JsonObject json, TypedMap context) {
    return constructor.apply(
      fieldA.get(json, context),
      fieldB.get(json, context),
      fieldC.get(json, context),
      fieldD.get(json, context),
      this
    );
  }

  @Override
  public void serializeInto(R object, JsonObject json, TypedMap context) {
    fieldA.serializeInto(object, json, context);
    fieldB.serializeInto(object, json, context);
    fieldC.serializeInto(object, json, context);
    fieldD.serializeInto(object, json, context);
  }

  @Override
  public R decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return constructor.apply(
      fieldA.decode(buffer, context),
      fieldB.decode(buffer, context),
      fieldC.decode(buffer, context),
      fieldD.decode(buffer, context),
      this
    );
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, R object, TypedMap context) {
    fieldA.encode(buffer, object, context);
    fieldB.encode(buffer, object, context);
    fieldC.encode(buffer, object, context);
    fieldD.encode(buffer, object, context);
  }
}
