package mantle.blocks.abstracts;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;


/**
 * 
 * @author progwml6
 * base class for itemBlocks with different unlocalized names based on metadata
 */

public class MultiItemBlock extends ItemBlock
{
    protected static String blockType[];
    protected static String unlocalizedName;

    public MultiItemBlock(int id, String itemBlockUnlocalizedName, String[] blockTypes)
    {
        super(id);
        this.unlocalizedName = itemBlockUnlocalizedName;
        this.blockType = blockTypes;
    }

    public int getMetadata (int meta)
    {
        return meta;
    }

    public String getUnlocalizedName (ItemStack itemstack)
    {
        int pos = MathHelper.clamp_int(itemstack.getItemDamage(), 0, blockType.length - 1);
        return (new StringBuilder()).append(unlocalizedName + ".").append(blockType[pos]).toString();
    }

}
