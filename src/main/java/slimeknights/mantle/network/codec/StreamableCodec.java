package slimeknights.mantle.network.codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import slimeknights.mantle.data.loadable.Streamable;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.mantle.util.typed.TypedMapBuilder;

import java.util.function.Consumer;

/**
 * Creates a {@link StreamCodec} from a {@link Streamable}.
 * @see slimeknights.mantle.data.loadable.common.CodecStreamable
 */
public record StreamableCodec<T>(Streamable<T> streamable, Consumer<TypedMapBuilder> context) implements StreamCodec<RegistryFriendlyByteBuf,T> {
  public StreamableCodec(Streamable<T> streamable) {
    this(streamable, TypedMapBuilder.EMPTY_CONSUMER);
  }

  /** Creates the context for the given buffer */
  private TypedMap createContext(RegistryFriendlyByteBuf buffer) {
    TypedMapBuilder builder = ContextKey.lookupContext(buffer.registryAccess());
    context.accept(builder);
    return builder.build();
  }

  @Override
  public T decode(RegistryFriendlyByteBuf buffer) {
    return streamable.decode(buffer, createContext(buffer));
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, T value) {
    streamable.encode(buffer, value, createContext(buffer));
  }
}
