package slimeknights.mantle.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Recipe that has an output other than an {@link ItemStack}
 * @param <I>  Input type
 */
public interface ICustomOutputRecipe<I extends RecipeInput> extends ICommonRecipe<I> {
  /** @deprecated Item stack output not supported */
  @Override
  @Deprecated
  default ItemStack getResultItem(HolderLookup.Provider access) {
    return ItemStack.EMPTY;
  }

  /** @deprecated Item stack output not supported */
  @Override
  @Deprecated
  default ItemStack assemble(I inv, HolderLookup.Provider access) {
    return ItemStack.EMPTY;
  }
}
