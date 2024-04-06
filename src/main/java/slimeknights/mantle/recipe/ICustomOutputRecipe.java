package slimeknights.mantle.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Recipe that has an output other than an {@link ItemStack}
 * @param <C>  Inventory type
 */
public interface ICustomOutputRecipe<C extends Container> extends ICommonRecipe<C> {
  /** @deprecated Item stack output not supported */
  @Override
  @Deprecated
  default ItemStack getResultItem(RegistryAccess access) {
    return ItemStack.EMPTY;
  }

  /** @deprecated Item stack output not supported */
  @Override
  @Deprecated
  default ItemStack assemble(C inv, RegistryAccess access) {
    return ItemStack.EMPTY;
  }
}
