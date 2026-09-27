package slimeknights.mantle.data;

import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

/**
 * Same as {@link com.mojang.serialization.MapCodec#unit(Supplier)} but for stream codecs.
 * @param constructor  Constructor to be called on decode. Called each time.
 *                     If you want the value to be singleton, consider {@link cpw.mods.util.Lazy} or {@link StreamCodec#unit(Object)}.
 */
public record SupplierStreamCodec<B,T>(Supplier<T> constructor) implements StreamCodec<B,T> {
  @Override
  public T decode(B buffer) {
    return constructor.get();
  }

  @Override
  public void encode(B buffer, T value) {}
}
