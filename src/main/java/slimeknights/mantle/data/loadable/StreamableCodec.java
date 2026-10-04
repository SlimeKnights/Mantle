package slimeknights.mantle.data.loadable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.function.Supplier;

/** Creates a {@link StreamCodec} from a {@link Streamable}. */
public record StreamableCodec<T>(Streamable<T> streamable, Supplier<TypedMap> context) implements StreamCodec<RegistryFriendlyByteBuf,T> {
  public StreamableCodec(Streamable<T> streamable) {
    this(streamable, TypedMap.EMPTY_SUPPLIER);
  }

  @Override
  public T decode(RegistryFriendlyByteBuf buffer) {
    return streamable.decode(buffer, context.get());
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, T value) {
    streamable.encode(buffer, value, context.get());
  }
}
