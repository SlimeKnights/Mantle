package mantle.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameData;

public class BlockUtils
{
    public static Block getBlockFromItem (Item item)
    {
        return Block.getBlockFromItem(item);
    }

    public static Block getBlockFromItemStack (ItemStack itemStack)
    {
        return getBlockFromItem(itemStack.getItem());
    }

    public static String getUniqueName (Block block)
    {
        return GameData.blockRegistry.getNameForObject(block);
    }

    public static Block getBlockFromUniqueName (String uniqueName)
    {
        return GameData.blockRegistry.getObject(uniqueName);
    }

}
