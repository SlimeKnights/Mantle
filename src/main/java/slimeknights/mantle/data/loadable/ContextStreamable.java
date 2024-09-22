package slimeknights.mantle.data.loadable;

import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.field.DirectField;
import slimeknights.mantle.util.typed.TypedMap;

/**
 * Streamable with additional context that can be passed from the parent during deserializing, notably used for recipe ID.
 * It is undecided how this will be translated to {@code StreamCodecs} once they exist
 */
public interface ContextStreamable<T> extends Streamable<T> {
  /**
   * Decodes this loadable from the network
   * @param buffer  Buffer instance
   * @param context  Additional parsing context, used notably by recipe serializers to store the ID and serializer.
   *                 Will be {@link TypedMap#EMPTY} in nested usages unless {@link DirectField} is used.
   * @return  Parsed object
   * @throws io.netty.handler.codec.DecoderException  If unable to decode
   */
  T decode(FriendlyByteBuf buffer, TypedMap context);

  /** Contextless implementation of {@link #decode(FriendlyByteBuf, TypedMap)} for {@link Streamable}. */
  @Override
  default T decode(FriendlyByteBuf buffer) {
    return decode(buffer, TypedMap.empty());
  }
}
