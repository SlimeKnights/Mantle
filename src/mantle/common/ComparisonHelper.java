package mantle.common;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ComparisonHelper
{
    public static boolean areEquivalent (Item item, Block block)
    {
        return item.equals(Item.getItemFromBlock(block));
    }

    public static boolean areEquivalent (Block block1, Block block2)
    {
        return block1.equals(block2);
    }

    public static boolean areEquivalent (Item item1, Item item2)
    {
        return item1.equals(item2);
    }

    public static boolean areEquivalent (ItemStack is1, ItemStack is2)
    {
        // TODO - do you want to check metadata as well? Or NBT tag existence?
        return is1.getItem().equals(is2.getItem());
    }
}
