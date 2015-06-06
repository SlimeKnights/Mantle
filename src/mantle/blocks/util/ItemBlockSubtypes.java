package mantle.blocks.util;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockSubtypes extends ItemBlock
{
    private final IBlockWithSubtypes blockWithSubtypes;

    public ItemBlockSubtypes(Block block)
    {
        super(block);

        this.blockWithSubtypes = (IBlockWithSubtypes) block;

        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage)
    {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        return super.getUnlocalizedName() + "." + this.blockWithSubtypes.getSubtypeUnlocalizedName(stack);
    }

}
