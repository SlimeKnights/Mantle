package slimeknights.mantle.data.loadable.field;

import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.data.loadable.Streamable;
import slimeknights.mantle.data.loadable.record.RecordJsonDeserializer;
import slimeknights.mantle.data.loadable.record.RecordJsonSerializer;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.function.Function;

/**
 * Simple implementation of a loadable field using record serializers.
 * @see slimeknights.mantle.data.loadable.CompositeLoadable
 * @see slimeknights.mantle.data.loadable.mapping.SimpleRecordLoadable
 */
public record CompositeLoadableField<T,P>(RecordJsonDeserializer<? extends T> deserializer, RecordJsonSerializer<? super T> serializer, Streamable<T> streamable, Function<P, ? extends T> getter) implements RecordField<T,P> {
  @Override
  public T get(JsonObject json, TypedMap context) {
    return deserializer.deserialize(json, context);
  }

  @Override
  public void serializeInto(P parent, JsonObject json, TypedMap context) {
    serializer.serializeInto(getter.apply(parent), json, context);
  }

  @Override
  public T decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return streamable.decode(buffer, context);
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, P parent, TypedMap context) {
    streamable.encode(buffer, getter.apply(parent), context);
  }
}
