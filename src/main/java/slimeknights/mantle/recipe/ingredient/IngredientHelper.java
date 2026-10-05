package slimeknights.mantle.recipe.ingredient;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/** Helpers for creating ingredients. Largely bringing back lost but useful constructors from the removed Mantle ingredients. */
public class IngredientHelper {
  /** Creates a new sized ingredient from the given items with the given size. */
  public static SizedIngredient sized(int size, ItemLike... items) {
    return new SizedIngredient(Ingredient.of(items), size);
  }

  /** Creates a new sized ingredient from the given stack. */
  public static SizedIngredient sized(ItemStack stack) {
    return new SizedIngredient(Ingredient.of(stack), stack.getCount());
  }
}
