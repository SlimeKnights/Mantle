package slimeknights.mantle.item.burnable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;

/**
 * Sign item with a burn time. Used in {@link slimeknights.mantle.registration.object.WoodBlockObject} registration.
 * @see BurnableBlockItem
 * @see BurnableHangingSignItem
 */
public class BurnableSignItem extends SignItem {
  private final int burnTime;
  public BurnableSignItem(Properties properties, Block floorBlock, Block wallBlock, int burnTime) {
    super(properties, floorBlock, wallBlock);
    this.burnTime = burnTime;
  }

  @Override
  public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
    return burnTime;
  }
}
