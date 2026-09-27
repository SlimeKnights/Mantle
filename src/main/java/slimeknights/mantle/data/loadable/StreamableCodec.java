package slimeknights.mantle.data.loadable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.function.Supplier;

/** Creates a {@link StreamCodec} from a {@link Streamable}. */
public record StreamableCodec<B extends FriendlyByteBuf, T>(Streamable<T> streamable, Supplier<TypedMap> context) implements StreamCodec<B,T> {
  public StreamableCodec(Streamable<T> streamable) {
    this(streamable, TypedMap.EMPTY_SUPPLIER);
  }

  @Override
  public T decode(B buffer) {
    return streamable.decode(buffer, context.get());
  }

  @Override
  public void encode(B buffer, T value) {
    streamable.encode(buffer, value);
  }
}
