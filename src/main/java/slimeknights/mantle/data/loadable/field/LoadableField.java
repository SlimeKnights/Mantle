package slimeknights.mantle.data.loadable.field;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;

/**
 * Interface for a field in a JSON object, typically used in {@link RecordLoadable} but also usable statically.
 * @param <P>  Parent object
 * @param <T>  Loadable type
 */
public interface LoadableField<T,P> extends RecordField<T,P> {
  /**
   * Gets the loadable from the given JSON
   * @param json  JSON object
   * @return  Parsed loadable value
   * @throws com.google.gson.JsonSyntaxException  If unable to read from JSON
   */
  T get(JsonObject json);

  @Override
  default T get(JsonObject json, TypedMap context) {
    return get(json);
  }

  /**
   * Parses this loadable from the network
   * @param buffer  Buffer instance
   * @return  Parsed field value
   * @throws io.netty.handler.codec.DecoderException  If unable to decode
   */
  T decode(FriendlyByteBuf buffer);

  @Override
  default T decode(FriendlyByteBuf buffer, TypedMap context) {
    return decode(buffer);
  }
}
