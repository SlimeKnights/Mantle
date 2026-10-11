package slimeknights.mantle.data.loadable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.data.loadable.record.RecordJsonDeserializer;
import slimeknights.mantle.data.loadable.record.RecordJsonSerializer;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;

/**
 * Helper for creating simple loadable implementations using a JSON deserializer, serializer, and a network streamable.
 * @see slimeknights.mantle.data.loadable.field.CompositeLoadableField
 * @see slimeknights.mantle.data.loadable.mapping.SimpleRecordLoadable
 */
public sealed interface CompositeLoadable<T> extends Streamable<T> {
  /** Creates a standard loadable from the given components. */
  static <T> Loadable<T> of(SimpleJsonDeserializer<? extends T> deserializer, SimpleJsonSerializer<? super T> serializer, Streamable<T> streamable) {
    return new Base<>(deserializer, serializer, streamable);
  }

  /** Replaces the network component of the passed loadable with the passed streamable. */
  static <T> Loadable<T> of(Loadable<T> loadable, Streamable<T> streamable) {
    return new Base<>(loadable, loadable, streamable);
  }

  /** Creates a record loadable from the given components. */
  static <T> RecordLoadable<T> of(RecordJsonDeserializer<? extends T> deserializer, RecordJsonSerializer<? super T> serializer, Streamable<T> streamable) {
    return new Record<>(deserializer, serializer, streamable);
  }

  /** Replaces the network component of the passed record loadable with the passed streamable. */
  static <T> RecordLoadable<T> of(RecordLoadable<T> loadable, Streamable<T> streamable) {
    return new Record<>(loadable, loadable, streamable);
  }


  /** Streamable handling the network side */
  Streamable<T> streamable();

  @Override
  default T decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return streamable().decode(buffer, context);
  }

  @Override
  default void encode(RegistryFriendlyByteBuf buffer, T value, TypedMap context) {
    streamable().encode(buffer, value, context);
  }

  /** Implementation for a record loadable using record serializers */
  record Base<T>(SimpleJsonDeserializer<? extends T> deserializer, SimpleJsonSerializer<? super T> serializer, Streamable<T> streamable) implements Loadable<T>, CompositeLoadable<T> {
    @Override
    public T convert(JsonElement element, String key, TypedMap context) {
      return deserializer.convert(element, key, context);
    }

    @Override
    public JsonElement serialize(T object, TypedMap context) {
      return serializer.serialize(object, context);
    }
  }

  /** Implementation for a record loadable using record serializers */
  record Record<T>(RecordJsonDeserializer<? extends T> deserializer, RecordJsonSerializer<? super T> serializer, Streamable<T> streamable) implements RecordLoadable<T>, CompositeLoadable<T> {
    @Override
    public T deserialize(JsonObject json, TypedMap context) {
      return deserializer.deserialize(json, context);
    }

    @Override
    public T convert(JsonElement element, String key, TypedMap context) {
      return deserializer.convert(element, key, context);
    }

    @Override
    public void serializeInto(T object, JsonObject json, TypedMap context) {
      serializer.serializeInto(object, json, context);
    }

    @Override
    public JsonElement serialize(T object, TypedMap context) {
      return serializer.serialize(object, context);
    }
  }
}
