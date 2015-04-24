package mantle.blocks.abstracts;

import static mantle.lib.CoreRepo.logger;
import net.minecraft.block.Block;
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
    private String blockType[];

    private String unlocalizedName;

    private String append;

    private int specialIndex[] = { -1, -1 };

    public MultiItemBlock(Block b, String itemBlockUnlocalizedName, String[] blockTypes)
    {
        super(b);
        if (itemBlockUnlocalizedName.isEmpty())
        {
            this.unlocalizedName = super.getUnlocalizedName();
        }
        else
        {
            this.unlocalizedName = itemBlockUnlocalizedName;
        }
        this.blockType = blockTypes;
        this.append = "";
    }

    public MultiItemBlock(Block b, String itemBlockUnlocalizedName, String appendToEnd, String[] blockTypes)
    {
        super(b);
        this.unlocalizedName = itemBlockUnlocalizedName;
        this.blockType = blockTypes;
        this.append = "." + appendToEnd;
    }

    public void setSpecialIndex(int clampIndex, int stringBuilderIndex)
    {
        this.specialIndex[0] = clampIndex;
        this.specialIndex[1] = stringBuilderIndex;
    }

    @Override
    public int getMetadata(int meta)
    {
        return meta;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack)
    {

        int pos = MathHelper.clamp_int(itemstack.getItemDamage(), 0, (this.specialIndex[0] > -1) ? this.specialIndex[0] : (this.blockType.length - 1));
        int sbIndex = (this.specialIndex[1] > -1) ? pos : (this.specialIndex[1] - pos);
        if (sbIndex < 0)
        {
            sbIndex = -1 * sbIndex;
        }
        try
        {
            return (new StringBuilder()).append(this.unlocalizedName).append(".").append(this.blockType[sbIndex - 1]).append(this.append).toString();
        }
        catch (ArrayIndexOutOfBoundsException ex)
        {
            logger.warn("[MultiItemBlock] Caught array index error in getUnlocalizedName: " + ex.getMessage());
            logger.warn("[MultiItemBlock] Returning unlocalized name: " + this.getUnlocalizedName());
            return this.getUnlocalizedName();
        }
    }

}
