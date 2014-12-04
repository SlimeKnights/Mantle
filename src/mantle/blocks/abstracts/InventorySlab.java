package mantle.blocks.abstracts;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Slab version of InventoryBlock.
 *
 * @author mDiyo
 */
public abstract class InventorySlab extends InventoryBlock
{
    public InventorySlab(Material material)
    {
        super(material);
    }

    /* Rendering */
    @Override
    public boolean renderAsNormalBlock ()
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube ()
    {
        return false;
    }

    @Override
    public boolean shouldSideBeRendered (IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        if (side > 1)
            return super.shouldSideBeRendered(world, pos, side);

        int meta = world.getBlockMetadata(pos);
        boolean top = (meta | 8) == 1;
        if ((top && side == 0) || (!top && side == 1))
            return true;

        return super.shouldSideBeRendered(world, pos, side);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void addCollisionBoxesToList (World world, BlockPos pos, IBlockState state, AxisAlignedBB axisalignedbb, List arraylist, Entity entity)
    {
        setBlockBoundsBasedOnState(world, pos);
        super.addCollisionBoxesToList(world,pos ,state,  axisalignedbb, arraylist, entity);
    }

    public void setBlockBoundsForItemRender ()
    {
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
    }

    public void setBlockBoundsBasedOnState (IBlockAccess world, BlockPos pos, IBlockState state)
    {
        int meta = world.getBlockState(pos) / 8;
        float minY = meta == 1 ? 0.5F : 0.0F;
        float maxY = meta == 1 ? 1.0F : 0.5F;
        setBlockBounds(0.0F, minY, 0F, 1.0F, maxY, 1.0F);
    }

    public int onBlockPlaced (World par1World, int blockX, int blockY, int blockZ, int side, float clickX, float clickY, float clickZ, int metadata)
    {
        if (side == 1)
            return metadata;
        if (side == 0 || clickY >= 0.5F)
            return metadata | 8;

        return metadata;
    }

    public int damageDropped (int meta)
    {
        return meta % 8;
    }
}