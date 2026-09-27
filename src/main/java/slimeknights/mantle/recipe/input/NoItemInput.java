package slimeknights.mantle.recipe.input;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/** Extension of {@link RecipeInput} with no items. */
public interface NoItemInput extends RecipeInput {
  /** Empty recipe input with no items or non-items */
  NoItemInput EMPTY = new NoItemInput() {};

  @Override
  default ItemStack getItem(int i) {
    return ItemStack.EMPTY;
  }

  @Override
  default int size() {
    return 0;
  }

  @Override
  default boolean isEmpty() {
    return true;
  }
}
