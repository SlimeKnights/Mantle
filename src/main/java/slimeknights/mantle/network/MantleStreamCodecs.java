package slimeknights.mantle.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.HashSet;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

/** Extra stream codecs used by Mantle for packets and alike */
public class MantleStreamCodecs {
  private MantleStreamCodecs() {}

  /** Stream codec for player hands. */
  public static final StreamCodec<ByteBuf, InteractionHand> INTERACTION_HAND = enumCodec(InteractionHand.class, OutOfBoundsStrategy.ZERO);
  /** Stream codec for a block */
  public static final StreamCodec<RegistryFriendlyByteBuf, Block> BLOCK = ByteBufCodecs.registry(Registries.BLOCK);
  /** Stream codec for an item */
  public static final StreamCodec<RegistryFriendlyByteBuf, Item> ITEM = ByteBufCodecs.registry(Registries.ITEM);
  /** Stream codec for a fluid */
  public static final StreamCodec<RegistryFriendlyByteBuf, Fluid> FLUID = ByteBufCodecs.registry(Registries.FLUID);
  /** Stream codec for a potion holder */
  public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Potion>> POTION = ByteBufCodecs.holderRegistry(Registries.POTION);


  /* Helpers */

  /** Gets the ordinal for an enum */
  private static final ToIntFunction<Enum<?>> ORDINAL = Enum::ordinal;

  /** Gets the ordinal getter */
  @SuppressWarnings("unchecked")
  private static <T extends Enum<T>> ToIntFunction<T> ordinal() {
    return (ToIntFunction<T>) ORDINAL;
  }

  /** Creates a codec mapping the enum ordinal to its value with the given out of bounds behavior. */
  public static <T extends Enum<T>> StreamCodec<ByteBuf, T> enumCodec(Class<T> enumClass, OutOfBoundsStrategy outOfBounds) {
    ToIntFunction<T> getId = Enum::ordinal;
    IntFunction<T> byID = ByIdMap.continuous(getId, enumClass.getEnumConstants(), outOfBounds);
    return ByteBufCodecs.idMapper(byID, getId);
  }

  /** Creates a codec for a set of elements */
  public static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, Set<V>> set() {
    return codec -> ByteBufCodecs.collection(HashSet::new, codec);
  }

  public static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, Set<V>> set(int maxSize) {
    return codec -> ByteBufCodecs.collection(HashSet::new, codec, maxSize);
  }
}
