package mantle.common;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ComparisonHelper
{
    public static boolean areEquivalent (Item item, Block block)
    {
        return item == Item.getItemFromBlock(block);
    }

    public static boolean areEquivalent (Block block1, Block block2)
    {
        return block1 == block2;
    }

    public static boolean areEquivalent (Item item1, Item item2)
    {
        return item1 == item2;
    }

    //@TODO make this a more verbose comparison
    public static boolean areEquivalent (ItemStack is1, ItemStack is2)
    {
        return is1.getItem() ==  is2.getItem();
    }
}
