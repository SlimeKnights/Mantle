package slimeknights.mantle.recipe.container;

import net.minecraft.world.item.ItemStack;

/**
 * Base inventory for inventories that do not use items
 */
public interface IEmptyContainer extends IRecipeContainer {
  /** Empty inventory instance, for cases where a nonnull inventory is required */
  IEmptyContainer EMPTY = new IEmptyContainer() {};

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
