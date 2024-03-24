package slimeknights.mantle.data.loadable.record;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.field.RecordField;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.function.BiFunction;

/** Record loadable with 2 fields */
record RecordLoadable2<A,B,R>(
  RecordField<A,? super R> fieldA,
  RecordField<B,? super R> fieldB,
  BiFunction<A,B,R> constructor
) implements RecordLoadable<R> {
  @Override
  public R deserialize(JsonObject json, TypedMap<Object> context) {
    return constructor.apply(
      fieldA.get(json, context),
      fieldB.get(json, context)
    );
  }

  @Override
  public void serialize(R object, JsonObject json) {
    fieldA.serialize(object, json);
    fieldB.serialize(object, json);
  }

  @Override
  public R fromNetwork(FriendlyByteBuf buffer, TypedMap<Object> context) {
    return constructor.apply(
      fieldA.fromNetwork(buffer, context),
      fieldB.fromNetwork(buffer, context)
    );
  }

  @Override
  public void toNetwork(R object, FriendlyByteBuf buffer) {
    fieldA.toNetwork(object, buffer);
    fieldB.toNetwork(object, buffer);
  }
}
