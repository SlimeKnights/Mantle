package mantle.blocks.iface;


import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface ICrop
{
    public enum HarvestType
    {
        BREAK, USE, MACHINE
    }

    public static final List<ItemStack> NO_YIELD = Collections.emptyList();

    /**
     *
     * @param world
     * @param pos
     * @param type
     * @return A list of items harvested. If no items are returned, NO_YIELD should be passed.
     */
    public List<ItemStack> harvestCrop (IBlockAccess world, BlockPos pos, HarvestType type);

    public boolean isFullyGrown (IBlockAccess world, BlockPos pos);

    public boolean hasYield (IBlockAccess world, BlockPos pos);

    public void growthTick (IBlockAccess world, BlockPos pos);
}
