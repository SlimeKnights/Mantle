package slimeknights.mantle.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Extension of {@link Recipe} to set some methods that always set.
 * @param <I>  Input type
 */
public interface ICommonRecipe<I extends RecipeInput> extends Recipe<I> {
  @Override
  default ItemStack assemble(I input, HolderLookup.Provider provider) {
    return getResultItem(provider).copy();
  }

  /** @deprecated Means nothing outside crafting tables */
  @Deprecated
  @Override
  default boolean canCraftInDimensions(int width, int height) {
    return true;
  }

  /**
   * Returns true to hide this recipe from the recipe book. Needed until Forge has proper recipe book support.
   * @return  True
   */
  @Override
  default boolean isSpecial() {
    return true;
  }
}
