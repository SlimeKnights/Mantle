package slimeknights.mantle.item.burnable;

import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;

/**
 * Tall block item with a burn time, used for doors. Used in {@link slimeknights.mantle.registration.object.WoodBlockObject} registration.
 * @see BurnableBlockItem
 */
public class BurnableTallBlockItem extends DoubleHighBlockItem {
  private final int burnTime;
  public BurnableTallBlockItem(Block block, Properties builder, int burnTime) {
    super(block, builder);
    this.burnTime = burnTime;
  }

  @Override
  public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
    return burnTime;
  }
}
