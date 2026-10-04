package slimeknights.mantle.data.loadable.field;

import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;

/**
 * Interface for a field in a JSON object loaded from a single field, typically used in {@link RecordLoadable} but also usable statically.
 * @param <P>  Parent object
 * @param <T>  Loadable type
 */
public interface LoadableField<T,P> extends RecordField<T,P> {
  /** Key this field is loaded from. Other fields may be used for fallback */
  String key();

  /**
   * Gets the loadable from the given JSON, overriding the key
   * @param json     JSON object
   * @param key      Key to use instead of {@link #key()}. Used for legacy fallback fields.
   * @param context  Additional parsing context, used notably by recipe serializers to store the ID and serializer.
   *                 Will be {@link TypedMap#EMPTY} in nested usages unless {@link DirectField} is used.
   * @return  Parsed loadable value
   * @throws com.google.gson.JsonSyntaxException  If unable to read from JSON
   */
  T get(JsonObject json, String key, TypedMap context);

  @NonExtendable
  @Override
  default T get(JsonObject json, TypedMap context) {
    return get(json, key(), context);
  }

  /** Same as {@link #get(JsonObject, TypedMap)} but passes {@link TypedMap#EMPTY} for context. */
  @NonExtendable
  default T get(JsonObject json) {
    return get(json, TypedMap.EMPTY);
  }

  /** Same as {@link #serializeInto(Object, JsonObject, TypedMap)} but passes {@link TypedMap#EMPTY} for context. */
  @NonExtendable
  default void serializeInto(P parent, JsonObject json) {
    serializeInto(parent, json, TypedMap.EMPTY);
  }

  /** Same as {@link RecordField#decode(RegistryFriendlyByteBuf, TypedMap)} but passes {@link TypedMap#EMPTY} for context. */
  @NonExtendable
  default T decode(RegistryFriendlyByteBuf buffer) {
    return decode(buffer, TypedMap.EMPTY);
  }

  /** Same as {@link RecordField#encode(RegistryFriendlyByteBuf, Object, TypedMap)} but passes {@link TypedMap#EMPTY} for context. */
  @NonExtendable
  default void encode(RegistryFriendlyByteBuf buffer, P parent) {
    encode(buffer, parent, TypedMap.EMPTY);
  }
}
