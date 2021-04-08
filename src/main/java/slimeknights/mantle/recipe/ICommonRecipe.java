package slimeknights.mantle.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;

/**
 * Extension of {@link Recipe} to set some methods that always set.
 * @param <C>  Inventory type
 */
public interface ICommonRecipe<C extends Inventory> extends Recipe<C> {
  @Override
  default ItemStack craft(C inv) {
    return getOutput().copy();
  }

  /** @deprecated Means nothing outside of crafting tables */
  @Deprecated
  @Override
  default boolean fits(int width, int height) {
    return true;
  }

  /**
   * Returns true to hide this recipe from the recipe book. Needed until Forge has proper recipe book support.
   * @return  True
   */
  @Override
  default boolean isIgnoredInRecipeBook() {
    return true;
  }
}
