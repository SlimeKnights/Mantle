package mantle.blocks.util;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockVariants extends ItemBlock
{
    private final IBlockWithVariants blockWithVariants;

    public ItemBlockVariants(Block block)
    {
        super(block);

        this.blockWithVariants = (IBlockWithVariants) block;

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
        return super.getUnlocalizedName() + "." + this.blockWithVariants.getVariantNameFromStack(stack);
    }

}
