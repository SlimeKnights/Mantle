package slimeknights.mantle.recipe.container;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * {@link Container} extension for a recipe that only needs read access.
 * Used to control which slots an recipe gets and to prevent the need to implement IInventory to get the recipe.
 */
public interface IRecipeContainer extends Container {
  /* Unsupported operations */

  /** @deprecated unsupported method */
  @Deprecated
  @Override
  default ItemStack removeItem(int index, int count) {
    throw new UnsupportedOperationException();
  }

  /** @deprecated unsupported method */
  @Deprecated
  @Override
  default ItemStack removeItemNoUpdate(int index) {
    throw new UnsupportedOperationException();
  }

  /** @deprecated unsupported method */
  @Deprecated
  @Override
  default void setItem(int index, ItemStack stack) {
    throw new UnsupportedOperationException();
  }

  /** @deprecated unsupported method */
  @Deprecated
  @Override
  default void clearContent() {
    throw new UnsupportedOperationException();
  }

  /* Unused */

  /** @deprecated unused method */
  @Deprecated
  @Override
  default void setChanged() {}

  /** @deprecated unused method */
  @Deprecated
  @Override
  default boolean stillValid(Player player) {
    return true;
  }
}
