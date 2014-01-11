package mantle.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class BlockUtils
{
    public static Block getBlockFromItem (Item item)
    {
        return Block.func_149634_a(item);
    }
    public static Block getBlockFromItemStack (ItemStack itemStack)
    {
        return getBlockFromItem(itemStack.getItem());
    }

}
