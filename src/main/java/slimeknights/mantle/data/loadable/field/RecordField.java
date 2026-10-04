package slimeknights.mantle.data.loadable.field;

import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;

/**
 * Interface for fields in a {@link RecordLoadable}.
 * Unlike {@link LoadableField}, this interface is not designed for use outside loadables.
 * @param <P>  Parent object
 * @param <T>  Loadable type
 */
public interface RecordField<T,P> {
  /**
   * Gets the loadable from the given JSON
   * @param json     JSON object
   * @param context  Additional parsing context, used notably by {@link slimeknights.mantle.registration.object.IdAwareObject} to store ID or recipes to store the serializer.
   * @return  Parsed loadable value
   * @throws com.google.gson.JsonSyntaxException  If unable to read from JSON
   */
  T get(JsonObject json, TypedMap context);

  /**
   * Serializes the passed object into the JSON instance
   * @param json     JSON instance
   * @param parent   Object
   * @param context  Additional serialization context, used notably by data components to fetch {@link net.minecraft.core.HolderLookup.Provider}.
   * @throws RuntimeException  If unable to save the element
   */
  void serializeInto(P parent, JsonObject json, TypedMap context);

  /**
   * Parses this loadable from the network
   * @param buffer  Buffer instance
   * @param context  Additional parsing context, used notably by {@link slimeknights.mantle.registration.object.IdAwareObject} to store ID or recipes to store the serializer.
   * @return  Parsed field value
   * @throws io.netty.handler.codec.DecoderException  If unable to decode a value from network
   */
  T decode(RegistryFriendlyByteBuf buffer, TypedMap context);

  /**
   * Writes this field to the buffer
   * @param buffer  Buffer instance
   * @param parent  Parent to read values from
   * @param context  Additional serialization context, no notable uses but exists for parity.
   * @throws io.netty.handler.codec.EncoderException  If unable to encode a value to network
   */
  void encode(RegistryFriendlyByteBuf buffer, P parent, TypedMap context);
}
