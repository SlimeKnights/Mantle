package slimeknights.mantle.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.util.RegistryHelper;

import java.util.List;
import java.util.function.Function;

/** This class contains codecs for various vanilla things that we need to use in codecs. */
public class MantleCodecs {
  /** Codec for a registry key */
  public static final Codec<ResourceKey<? extends Registry<?>>> REGISTRY_KEY = ResourceLocation.CODEC.xmap(ResourceKey::createRegistryKey, ResourceKey::location);
  /** Codec for a registry key */
  public static final MapCodec<ResourceKey<? extends Registry<?>>> REGISTRY_FIELD = REGISTRY_KEY.optionalFieldOf("registry", Registries.ITEM);

  /** Codec for a tag key from any registry */
  public static final MapCodec<TagKey<?>> TAG_KEY = RecordCodecBuilder.mapCodec(instance -> instance.group(
    REGISTRY_FIELD.forGetter(TagKey::registry),
    ResourceLocation.CODEC.fieldOf("tag").forGetter(TagKey::location)
    ).apply(instance, (registry, location) -> TagKey.create(RegistryHelper.castKey(registry), location)));
  /** Codec for a block tag */
  public static final Codec<TagKey<Block>> BLOCK_TAG = TagKey.codec(Registries.BLOCK);
  /** Codec for an item tag */
  public static final Codec<TagKey<Item>> ITEM_TAG = TagKey.codec(Registries.ITEM);
  /** Codec for a fluid tag */
  public static final Codec<TagKey<Fluid>> FLUID_TAG = TagKey.codec(Registries.FLUID);

  /** Creates a codec for a list that serializes to a single element when size 1 */
  private static <E> Codec<List<E>> compactList(Codec<E> codec, Codec<List<E>> listCodec) {
    return Codec.either(listCodec, codec).xmap(
      either -> either.map(Function.identity(), List::of),
      list -> list.size() == 1 ? Either.right(list.getFirst()) : Either.left(list)
    );
  }

  /** Creates a codec for a list that serializes to a single element when size 1. List size may be as small as 0. */
  public static <E> Codec<List<E>> compactOrEmptyList(Codec<E> codec) {
    return compactList(codec, codec.listOf());
  }

  /** Creates a codec for a list that serializes to a single element when size 1. List size must be at least 1. */
  public static <E> Codec<List<E>> compactList(Codec<E> codec) {
    return compactList(codec, codec.listOf(1, Integer.MAX_VALUE));
  }
}
