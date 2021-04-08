package slimeknights.mantle.recipe.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

/**
 * IInventory extension for a recipe that only needs read access.
 * Used to control which slots an recipe gets and to prevent the need to implement IInventory to get the recipe.
 */
public interface IReadOnlyInventory extends Inventory {
  /* Unsupported operations */

  /** @deprecated unsupported method */
  @Deprecated
  @Override
  default ItemStack removeStack(int index, int count) {
    throw new UnsupportedOperationException();
  }

  /** @deprecated unsupported method */
  @Deprecated
  @Override
  default ItemStack removeStack(int index) {
    throw new UnsupportedOperationException();
  }

  /** @deprecated unsupported method */
  @Deprecated
  @Override
  default void setStack(int index, ItemStack stack) {
    throw new UnsupportedOperationException();
  }

  /** @deprecated unsupported method */
  @Deprecated
  @Override
  default void clear() {
    throw new UnsupportedOperationException();
  }

  /* Unused */

  /** @deprecated unused method */
  @Deprecated
  @Override
  default void markDirty() {}

  /** @deprecated unused method */
  @Deprecated
  @Override
  default boolean canPlayerUse(PlayerEntity player) {
    return true;
  }
}
