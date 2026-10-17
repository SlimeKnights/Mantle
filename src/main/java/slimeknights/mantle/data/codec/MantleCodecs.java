package slimeknights.mantle.data.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
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

  /** Codec for a holder set of items */
  public static final Codec<HolderSet<Item>> ITEM_SET = holderSet(BuiltInRegistries.ITEM);
  /** Codec for a holder set of fluids */
  public static final Codec<HolderSet<Fluid>> FLUID_SET = holderSet(BuiltInRegistries.FLUID);
  /** Codec for a holder set of potions */
  public static final Codec<HolderSet<Potion>> POTION_SET = holderSet(BuiltInRegistries.POTION);
  /** Codec for a holder set of entity types */
  public static final Codec<HolderSet<EntityType<?>>> ENTITY_SET = holderSet(BuiltInRegistries.ENTITY_TYPE);

  /** Codec for a block that is not air */
  public static final Codec<Block> NON_AIR_BLOCK = notValue(BuiltInRegistries.BLOCK, Blocks.AIR);
  /** Codec for an item that is not air */
  public static final Codec<Item> NON_AIR_ITEM = notValue(BuiltInRegistries.ITEM, Items.AIR);
  /** Codec for a fluid that is not empty */
  public static final Codec<Fluid> NON_EMPTY_FLUID = notValue(BuiltInRegistries.FLUID, Fluids.EMPTY);

  /** Codec for the condition context from conditional ops. */
  public static final Codec<ICondition.IContext> CONDITION_CONTEXT = ConditionalOps.retrieveContext().codec();

  /** Codec for the list of ingredients in a shapeless crafting recipe */
  public static final Codec<List<Ingredient>> SHAPELESS_INGREDIENTS = Ingredient.CODEC_NONEMPTY.listOf(1, Short.MAX_VALUE).flatXmap(list -> {
    int max = ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth();
    if (list.size() > max) {
      return DataResult.error(() -> "Too many ingredients for shapeless recipe. The maximum is: " + max);
    } else {
      return DataResult.success(list);
    }
  }, DataResult::success);


  /* Helpers */

  /** Creates a holder set codec for the given registry. */
  public static <T> Codec<HolderSet<T>> holderSet(Registry<T> registry) {
    return HolderSetCodec.create(registry.key(), registry.holderByNameCodec(), false);
  }

  /** Codec for a registry that is not the default value. */
  public static <T> Codec<T> notValue(Registry<T> registry, T notValue) {
    String errorMsg = registry.key().location() + " must not be " + registry.getKey(notValue);
    return registry.byNameCodec().validate(value -> value == notValue ? DataResult.error(() -> errorMsg) : DataResult.success(value));
  }

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
