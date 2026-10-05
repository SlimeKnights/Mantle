package slimeknights.mantle.item.burnable;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;

/**
 * Block item with a burn time. Used in {@link slimeknights.mantle.registration.object.WoodBlockObject} registration.
 * @see BurnableSignItem
 * @see BurnableHangingSignItem
 * @see BurnableTallBlockItem
 */
public class BurnableBlockItem extends BlockItem {
  private final int burnTime;
  public BurnableBlockItem(Block block, Properties properties, int burnTime) {
    super(block, properties);
    this.burnTime = burnTime;
  }

  @Override
  public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
    return burnTime;
  }
}
