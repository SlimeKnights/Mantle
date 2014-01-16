package mantle.common;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ComparisonHelper
{
    public static boolean areEquivalent (Item item, Block block)
    {
        //TODO figure this out!!!
        return item.equals(block);
    }
    public static boolean areEquivalent (Block block1, Block block2)
    {
        //TODO figure this out!!!
        return block1.equals(block2);
    }
    public static boolean areEquivalent (Item item1, Item item2)
    {
        //TODO figure this out!!!
        return item1.equals(item2);
    }
    public static boolean areEquivalent (ItemStack is1, ItemStack is2)
    {
        //TODO figure this out!!!
        return is1.getItem().equals(is2.getItem());
    }
}
