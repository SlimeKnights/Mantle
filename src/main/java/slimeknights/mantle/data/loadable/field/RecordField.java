package slimeknights.mantle.data.loadable.field;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;

/**
 * Interface for fields in a {@link RecordLoadable}.
 * Unlike {@link LoadableField}, this interface is not designed for use outside of loadables.
 * @param <P>  Parent object
 * @param <T>  Loadable type
 */
public interface RecordField<T,P> {
  /**
   * Gets the loadable from the given JSON
   * @param json     JSON object
   * @param context  Additional parsing context, used notably by recipe serializers to store the ID and serializer.
   *                 Will be {@link TypedMap#EMPTY} in nested usages unless {@link DirectField} is used.
   * @return  Parsed loadable value
   * @throws com.google.gson.JsonSyntaxException  If unable to read from JSON
   */
  T get(JsonObject json, TypedMap context);

  /**
   * Serializes the passed object into the JSON instance
   * @param json    JSON instance
   * @param parent  Object
   * @throws RuntimeException  If unable to save the element
   */
  void serialize(P parent, JsonObject json);

  /**
   * Parses this loadable from the network
   * @param buffer  Buffer instance
   * @param context  Additional parsing context, used notably by recipe serializers to store the ID and serializer.
   *                 Will be {@link TypedMap#EMPTY} in nested usages unless {@link DirectField} is used.
   * @return  Parsed field value
   * @throws io.netty.handler.codec.DecoderException  If unable to decode a value from network
   */
  T decode(FriendlyByteBuf buffer, TypedMap context);

  /**
   * Writes this field to the buffer
   * @param buffer  Buffer instance
   * @param parent  Parent to read values from
   * @throws io.netty.handler.codec.EncoderException  If unable to encode a value to network
   */
  void encode(FriendlyByteBuf buffer, P parent);
}
