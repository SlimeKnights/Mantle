package mantle.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class BlockUtils
{
    public static Block getBlockFromItem (Item item)
    {
        return Block.func_149634_a(item);
    }

}
