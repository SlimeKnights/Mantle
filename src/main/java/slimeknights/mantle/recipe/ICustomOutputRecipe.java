package slimeknights.mantle.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Recipe that has an output other than an {@link ItemStack}
 * @param <C>  Inventory type
 */
public interface ICustomOutputRecipe<C extends RecipeInput> extends ICommonRecipe<C> {
  /** @deprecated Item stack output not supported */
  @Override
  @Deprecated
  default ItemStack getResultItem(HolderLookup.Provider access) {
    return ItemStack.EMPTY;
  }

  /** @deprecated Item stack output not supported */
  @Override
  @Deprecated
  default ItemStack assemble(C inv, HolderLookup.Provider access) {
    return ItemStack.EMPTY;
  }
}
