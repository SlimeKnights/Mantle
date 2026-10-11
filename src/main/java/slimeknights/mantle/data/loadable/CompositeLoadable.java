package slimeknights.mantle.data.loadable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;

/**
 * Helper for building a loadable from a loadable and a streamable.
 * @see slimeknights.mantle.data.loadable.mapping.SimpleRecordLoadable
 */
public sealed interface CompositeLoadable<T> extends Streamable<T> {
  /** Replaces the network component of the passed loadable with the passed streamable. */
  static <T> Loadable<T> of(Loadable<T> loadable, Streamable<T> streamable) {
    return new Base<>(loadable, streamable);
  }

  /** Replaces the network component of the passed record loadable with the passed streamable. */
  static <T> RecordLoadable<T> of(RecordLoadable<T> loadable, Streamable<T> streamable) {
    return new Record<>(loadable, streamable);
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
  record Base<T>(Loadable<T> loadable, Streamable<T> streamable) implements Loadable<T>, CompositeLoadable<T> {
    @Override
    public T convert(JsonElement element, String key, TypedMap context) {
      return loadable.convert(element, key, context);
    }

    @Override
    public JsonElement serialize(T object, TypedMap context) {
      return loadable.serialize(object, context);
    }
  }

  /** Implementation for a record loadable using record serializers */
  record Record<T>(RecordLoadable<T> loadable, Streamable<T> streamable) implements RecordLoadable<T>, CompositeLoadable<T> {
    @Override
    public T deserialize(JsonObject json, TypedMap context) {
      return loadable.deserialize(json, context);
    }

    @Override
    public T convert(JsonElement element, String key, TypedMap context) {
      return loadable.convert(element, key, context);
    }

    @Override
    public void serializeInto(T object, JsonObject json, TypedMap context) {
      loadable.serializeInto(object, json, context);
    }

    @Override
    public JsonElement serialize(T object, TypedMap context) {
      return loadable.serialize(object, context);
    }
  }
}
