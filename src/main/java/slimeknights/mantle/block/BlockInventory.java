package slimeknights.mantle.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

import slimeknights.mantle.tileentity.TileInventory;

// Updated Version of InventoryBlock in Mantle
public abstract class BlockInventory extends BlockContainer {

  protected BlockInventory(Material material) {
    super(material);
  }

  // inventories usually need a tileEntity
  @Override
  public boolean hasTileEntity(IBlockState state) {
    return true;
  }

  @Nonnull
  @Override
  public abstract TileEntity createNewTileEntity(@Nonnull World worldIn, int meta);

  /**
   * Called when the block is activated. Return true if a GUI is opened, false if the block has no GUI.
   */
  protected abstract boolean openGui(EntityPlayer player, World world, BlockPos pos);

  @Override
  public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack stack, EnumFacing side,
                                  float clickX, float clickY, float clickZ) {
    if(player.isSneaking()) {
      return false;
    }

    if(!world.isRemote) {
      return this.openGui(player, world, pos);
    }

    return true;
  }

  @Override
  public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer,
                              ItemStack stack) {
    super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

    // set custom name from named stack
    if(stack.hasDisplayName()) {
      TileEntity tileentity = worldIn.getTileEntity(pos);

      if(tileentity instanceof TileInventory) {
        ((TileInventory) tileentity).setCustomName(stack.getDisplayName());
      }
    }
  }

  @Override
  public void breakBlock(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
    TileEntity tileentity = worldIn.getTileEntity(pos);

    if(tileentity instanceof TileInventory) {
      InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory) tileentity);
      worldIn.updateComparatorOutputLevel(pos, this);
    }

    super.breakBlock(worldIn, pos, state);
  }

  @Override
  public int damageDropped(IBlockState state) {
    return getMetaFromState(state);
  }

  // BlockContainer sets this to invisible
  // we need model for standard forge rendering
  @Nonnull
  @Override
  public EnumBlockRenderType getRenderType(IBlockState state) {
    return EnumBlockRenderType.MODEL;
  }
}
