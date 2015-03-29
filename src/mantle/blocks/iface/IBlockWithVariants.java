package mantle.blocks.iface;

import net.minecraft.item.ItemStack;

public interface IBlockWithVariants
{
	String getVariantNameFromStack(ItemStack stack);
}
