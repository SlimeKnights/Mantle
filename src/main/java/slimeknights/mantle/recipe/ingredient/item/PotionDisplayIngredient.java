package slimeknights.mantle.recipe.ingredient.item;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
 * Ingredient that shows all potion variants on the displayed item list.
 * <p>
 * Matches any potion, even those with custom potion contents. If you need to just match real potions use {@link PotionIngredient} with {@link net.neoforged.neoforge.registries.holdersets.AnyHolderSet}.
 * @see PotionIngredient
 */
public record PotionDisplayIngredient(HolderSet<Item> items) implements ICustomIngredient {
  public static final MapCodec<PotionDisplayIngredient> CODEC = MantleCodecs.ITEM_SET.fieldOf("items").xmap(PotionDisplayIngredient::new, PotionDisplayIngredient::items);
  public static final StreamCodec<RegistryFriendlyByteBuf,PotionDisplayIngredient> STREAM_CODEC = MantleStreamCodecs.ITEM_SET.map(PotionDisplayIngredient::new, PotionDisplayIngredient::items);

  /** Creates an ingredient matching a list of items */
  public static Ingredient of(HolderSet<Item> items) {
    return new PotionDisplayIngredient(items).toVanilla();
  }

  /** Creates an ingredient matching a list of items */
  public static Ingredient of(List<ItemLike> items) {
    return of(IngredientHelper.itemSet(items));
  }

  /** Creates an ingredient matching a list of items */
  public static Ingredient of(ItemLike... items) {
    return of(List.of(items));
  }

  @Override
  public IngredientType<PotionDisplayIngredient> getType() {
    return MantleRecipes.POTION_DISPLAY_INGREDIENT.get();
  }

  @Override
  public boolean test(ItemStack stack) {
    return items.contains(stack.getItemHolder());
  }

  @Override
  public boolean isSimple() {
    // even though this is logically simple, Neo syncs simple ingredients as a list of items which makes it less efficient on matches
    return false;
  }

  @Override
  public Stream<ItemStack> getItems() {
    return BuiltInRegistries.POTION.holders()
      .flatMap(potion -> {
        PotionContents contents = new PotionContents(potion);
        return this.items.stream().map(item -> {
          ItemStack stack = new ItemStack(item);
          stack.set(DataComponents.POTION_CONTENTS, contents);
          return stack;
        });
      });
  }
}
