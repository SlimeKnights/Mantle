package slimeknights.mantle.recipe.data;

import net.minecraft.world.item.ItemStack;

/**
 * Simply an extension of the Forge ingredient because the constructor is not public for some dumb reason
 */
public class NBTIngredient extends net.minecraftforge.common.crafting.NBTIngredient {
  protected NBTIngredient(ItemStack stack) {
    super(stack);
  }

  public static NBTIngredient from(ItemStack stack) {
    return new NBTIngredient(stack);
  }
}
