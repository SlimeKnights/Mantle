package slimeknights.mantle.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;

import slimeknights.mantle.block.EnumBlockSlab;

public class ItemBlockSlab extends ItemBlockMeta {

  public EnumBlockSlab<?> slab;

  public ItemBlockSlab(EnumBlockSlab<?> block) {
    super(block);
    this.slab = block;
  }

  /**
   * Called when a Block is right-clicked with this Item
   */
  @Override
  public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
    // don't place the slab if unable to edit
    if(stack.stackSize != 0 && player.canPlayerEdit(pos.offset(facing), facing, stack)) {

      // try placing the slab at the current position
      // note that this requires the slab to be extended on the side the block was clicked
      if(tryPlace(player, stack, world, pos, facing)) {
        return EnumActionResult.SUCCESS;
      }
      // otherwise. try and place it in the block in front
      else if(this.tryPlace(player, stack, world, pos.offset(facing), null)) {
        return EnumActionResult.SUCCESS;
      }

      return super.onItemUse(stack, player, world, pos, hand, facing, hitX, hitY, hitZ);
    }
    else {
      return EnumActionResult.FAIL;
    }
  }

  @Override
  @SideOnly(Side.CLIENT)
  public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
    BlockPos oldPos = pos;
    Comparable<?> type = this.slab.getTypeForItem(stack);
    IBlockState state = world.getBlockState(pos);

    // first, try placing on the same block
    if(state.getBlock() == this.slab) {
      boolean flag = state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP;

      if((side == EnumFacing.UP && !flag || side == EnumFacing.DOWN && flag) && type == state.getValue(this.slab.prop) && this.slab.getFullBlock(state) != null) {
        return true;
      }
    }

    // if that does not work, offset by one and try same type
    pos = pos.offset(side);
    state = world.getBlockState(pos);
    return state.getBlock() == this.slab && type == state.getValue(this.slab.prop) || super.canPlaceBlockOnSide(world, oldPos, side, player, stack);
  }

  private boolean tryPlace(EntityPlayer player, ItemStack stack, World worldIn, BlockPos pos, EnumFacing facing) {
    IBlockState state = worldIn.getBlockState(pos);
    Comparable<?> type = this.slab.getTypeForItem(stack);

    if(state.getBlock() == this.slab) {
      BlockSlab.EnumBlockHalf half = state.getValue(BlockSlab.HALF);

      if(type == state.getValue(this.slab.prop)
         && (facing == null
             || facing == EnumFacing.UP && half == BlockSlab.EnumBlockHalf.BOTTOM
             || facing == EnumFacing.DOWN && half == BlockSlab.EnumBlockHalf.TOP)) {

        IBlockState fullBlock = this.slab.getFullBlock(state);
        if(fullBlock != null) {
          AxisAlignedBB axisalignedbb = fullBlock.getCollisionBoundingBox(worldIn, pos);

          if(axisalignedbb != Block.NULL_AABB && worldIn.checkNoEntityCollision(axisalignedbb.offset(pos)) && worldIn.setBlockState(pos, fullBlock, 11)) {
            SoundType soundtype = fullBlock.getBlock().getSoundType();
            worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            --stack.stackSize;
          }

          return true;
        }
      }
    }

    return false;
  }

  // adds the "half=bottom" to the item model, so it does not error
  @Override
  @SuppressWarnings("unchecked")
  @SideOnly(Side.CLIENT)
  public void registerItemModels() {
    final Item item = this;
    final ResourceLocation loc = GameData.getBlockRegistry().getNameForObject(block);

    for(Comparable<?> o : (Collection<Comparable<?>>) mappingProperty.getAllowedValues()) {
      int meta = block.getMetaFromState(block.getDefaultState().withProperty(mappingProperty, o));
      String name = mappingProperty.getName(o);

      ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(loc, "half=bottom," + mappingProperty.getName() + "=" + name));
    }
  }

}
