package mantle.blocks.util;

import net.minecraft.item.ItemStack;

public interface IBlockWithVariants
{
	String getVariantNameFromStack(ItemStack stack);
}