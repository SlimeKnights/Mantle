package slimeknights.mantle.data.loadable.common;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import slimeknights.mantle.data.loadable.Streamable;
import slimeknights.mantle.util.typed.TypedMap;

/**
 * {@link Streamable} made from a {@link StreamCodec}
 * @see slimeknights.mantle.network.codec.StreamableCodec
 */
public record CodecStreamable<T>(StreamCodec<? super RegistryFriendlyByteBuf,T> codec) implements Streamable<T> {
  @Override
  public T decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return codec.decode(buffer);
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, T value, TypedMap context) {
    codec.encode(buffer, value);
  }
}
