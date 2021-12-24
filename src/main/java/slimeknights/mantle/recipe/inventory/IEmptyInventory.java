package slimeknights.mantle.recipe.inventory;

import net.minecraft.world.item.ItemStack;

/**
 * Base inventory for inventories that do not use items
 */
public interface IEmptyInventory extends IReadOnlyInventory {
  /** Empty inventory instance, for cases where a nonnull inventory is required */
  IEmptyInventory EMPTY = new IEmptyInventory() {};

  /** @deprecated unused method */
  @Deprecated
  @Override
  default ItemStack getItem(int index) {
    return ItemStack.EMPTY;
  }

  /** @deprecated unused method */
  @Deprecated
  @Override
  default boolean isEmpty() {
    return true;
  }

  /** @deprecated always 0, not useful */
  @Deprecated
  @Override
  default int getContainerSize() {
    return 0;
  }
}
