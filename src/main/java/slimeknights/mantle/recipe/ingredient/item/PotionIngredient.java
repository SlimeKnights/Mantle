package slimeknights.mantle.recipe.ingredient.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.codec.MantleCodecs;
import slimeknights.mantle.network.codec.MantleStreamCodecs;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.recipe.ingredient.IngredientHelper;

import java.util.List;
import java.util.stream.Stream;

/**
 * Simple ingredient checking for an item with a specific potion.
 * @see PotionDisplayIngredient
 * @see slimeknights.mantle.recipe.ingredient.fluid.PotionFluidIngredient
 */
public record PotionIngredient(HolderSet<Potion> potion, HolderSet<Item> items) implements ICustomIngredient {
  public static final MapCodec<PotionIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    MantleCodecs.POTION_SET.fieldOf("potion").forGetter(PotionIngredient::potion),
    MantleCodecs.ITEM_SET.fieldOf("items").forGetter(PotionIngredient::items)
  ).apply(instance, PotionIngredient::new));
  public static final StreamCodec<RegistryFriendlyByteBuf,PotionIngredient> STREAM_CODEC = StreamCodec.composite(
    MantleStreamCodecs.POTION_SET, PotionIngredient::potion,
    MantleStreamCodecs.ITEM_SET, PotionIngredient::items,
    PotionIngredient::new);

  /** Creates a potion ingredient matching a list of items */
  public static Ingredient of(HolderSet<Potion> potion, HolderSet<Item> items) {
    return new PotionIngredient(potion, items).toVanilla();
  }

  /** Creates a potion ingredient matching a list of items */
  public static Ingredient of(Holder<Potion> potion, HolderSet<Item> items) {
    return of(HolderSet.direct(potion), items);
  }

  /** Creates a potion ingredient matching a list of items */
  public static Ingredient of(Holder<Potion> potion, List<ItemLike> items) {
    return of(potion, IngredientHelper.itemSet(items));
  }

  /** Creates a potion ingredient matching a list of items */
  public static Ingredient of(Holder<Potion> potion, ItemLike... items) {
    return of(potion, List.of(items));
  }

  /** Creates a potion ingredient matching a tag */
  public static Ingredient of(Holder<Potion> potion, TagKey<Item> tag) {
    return of(potion, IngredientHelper.itemSet(tag));
  }

  @Override
  public IngredientType<PotionIngredient> getType() {
    return MantleRecipes.POTION_INGREDIENT.get();
  }

  /** Checks if the given contents matches the given potions */
  public static boolean matches(PotionContents contents, HolderSet<Potion> potions) {
    if (!contents.customEffects().isEmpty()) {
      return false;
    }
    Holder<Potion> holder = contents.potion().orElse(null);
    return holder != null && potions.contains(holder);
  }

  /** Checks if the given stack matches the given potions */
  public static boolean matches(DataComponentHolder stack, HolderSet<Potion> potions) {
    return matches(stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY), potions);
  }

  @Override
  public boolean test(ItemStack stack) {
    // must match an item, and potion must match
    return items.contains(stack.getItemHolder()) && matches(stack, potion);
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public Stream<ItemStack> getItems() {
    return potion.stream().flatMap(potion -> {
      PotionContents contents = new PotionContents(potion);
      return items.stream().map(item -> {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.POTION_CONTENTS, contents);
        return stack;
      });
    });
  }
}
