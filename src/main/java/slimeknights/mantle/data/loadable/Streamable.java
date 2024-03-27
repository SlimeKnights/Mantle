package slimeknights.mantle.data.loadable;

import net.minecraft.network.FriendlyByteBuf;

/** This is a temporary interface intended to be replaced by Mojang's {@code StreamCodec} in the future. It means loadables will automatically work as stream codecs with minimal extra effort. */
public interface Streamable<T> {
  /**
   * Reads the object from the packet buffer
   * @param buffer  Buffer instance
   * @return  Instance read from network
   * @throws io.netty.handler.codec.DecoderException  If unable to decode a value from network
   */
  T decode(FriendlyByteBuf buffer);

  /**
   * Writes this object to the packet buffer
   * @param buffer  Buffer instance
   * @param value  Object to write
   * @throws io.netty.handler.codec.EncoderException  If unable to encode a value to network
   */
  void encode(FriendlyByteBuf buffer, T value);
}
