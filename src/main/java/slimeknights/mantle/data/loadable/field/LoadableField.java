package slimeknights.mantle.data.loadable.field;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

/**
 * Interface for fields in a {@link RecordLoadable}
 * @param <P>  Parent object
 * @param <T>  Loadable type
 */
public interface LoadableField<T,P> {
  /**
   * Gets the loadable from the given JSON
   * @param json  JSON object
   * @return  Parsed loadable value
   * @throws com.google.gson.JsonSyntaxException  If unable to read from JSON
   */
  T get(JsonObject json);

  /**
   * Serializes the passed object into the JSON instance
   * @param json    JSON instance
   * @param parent  Parent being serialized
   * @throws RuntimeException  If unable to save the element
   */
  void serialize(P parent, JsonObject json);

  /**
   * Parses this loadable from the network
   * @param buffer  Buffer instance
   * @return  Parsed field value
   * @throws io.netty.handler.codec.DecoderException  If unable to decode
   */
  T fromNetwork(FriendlyByteBuf buffer);

  /**
   * Writes this field to the buffer
   * @param parent  Parent to read values from
   * @param buffer  Buffer instance
   * @throws io.netty.handler.codec.EncoderException  If unable to write a value
   */
  void toNetwork(P parent, FriendlyByteBuf buffer);
}
