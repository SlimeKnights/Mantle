package slimeknights.mantle.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.block.entity.IRetexturedBlockEntity;
import slimeknights.mantle.util.RetexturedHelper;

import java.util.List;

/**
 * Logic for a retexturable block. Use alongside {@link IRetexturedBlockEntity} and {@link RetexturedHelper}
 */
@SuppressWarnings("WeakerAccess")
public abstract class RetexturedBlock extends Block implements EntityBlock {
  public RetexturedBlock(Properties properties) {
    super(properties);
  }

  @SuppressWarnings("deprecation")
  @Override
  public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
    return getPickBlock(level, pos, state);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    RetexturedHelper.addTooltip(stack, tooltip, flag);
  }


  /* Utils */

  /**
   * Call in {@link Block#setPlacedBy(Level, BlockPos, BlockState, LivingEntity, ItemStack)} to set the texture tag to the Tile Entity.
   * @param world World where the block was placed
   * @param pos   Block position
   * @param stack Item stack
   * @deprecated no longer necessary. Copy the texture data over using {@link net.minecraft.world.level.block.entity.BlockEntity#collectImplicitComponents(Builder)}.
   */
  @Deprecated(forRemoval = true)
  public static void updateTextureBlock(Level world, BlockPos pos, ItemStack stack) {
    Block block = RetexturedHelper.getTexture(stack);
    if (block != Blocks.AIR && world.getBlockEntity(pos) instanceof IRetexturedBlockEntity te) {
      te.updateTexture(block);
    }
  }

  /**
   * Called in blocks to get the item stack for the current block
   * @param world World
   * @param pos   Pos
   * @param state State
   * @return Pickblock stack with proper NBT
   * @apiNote This is used to copy data to the item stack when pick block without control is called. For pick block with control and loot tables, your block entity should use {@link net.minecraft.world.level.block.entity.BlockEntity#collectImplicitComponents(Builder)}.
   */
  public static ItemStack getPickBlock(BlockGetter world, BlockPos pos, BlockState state) {
    Block block = state.getBlock();
    ItemStack stack = new ItemStack(block);
    if (world.getBlockEntity(pos) instanceof IRetexturedBlockEntity te) {
      RetexturedHelper.setTexture(stack, te.getTexture());
    }
    return stack;
  }
}
