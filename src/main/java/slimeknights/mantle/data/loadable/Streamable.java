package slimeknights.mantle.data.loadable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import slimeknights.mantle.util.typed.TypedMap;

/** This interface partially implements Mojang's future {@code StreamCodec} for the sake of ensuring all {@link Loadable} are automatically compatible with stream codecs. */
public interface Streamable<T> {
  /**
   * Decodes this loadable from the network
   * @param buffer  Buffer instance
   * @param context Additional parsing context, used notably by recipe serializers to store the ID and serializer.
   * @return  Parsed object
   * @throws io.netty.handler.codec.DecoderException  If unable to decode
   */
  T decode(RegistryFriendlyByteBuf buffer, TypedMap context);

  /** Same as {@link #decode(RegistryFriendlyByteBuf, TypedMap)} but passes {@link TypedMap#EMPTY} for context. */
  @NonExtendable
  default T decode(RegistryFriendlyByteBuf buffer) {
    return decode(buffer, TypedMap.EMPTY);
  }

  /**
   * Writes this object to the packet buffer
   * @param buffer  Buffer instance
   * @param value  Object to write
   * @throws io.netty.handler.codec.EncoderException  If unable to encode a value to network
   */
  void encode(RegistryFriendlyByteBuf buffer, T value);
}
