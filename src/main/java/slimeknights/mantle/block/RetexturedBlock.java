package slimeknights.mantle.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.block.entity.IRetexturedBlockEntity;
import slimeknights.mantle.util.BlockEntityHelper;
import slimeknights.mantle.util.RetexturedHelper;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Logic for a retexturable block. Use alongside {@link IRetexturedBlockEntity} and {@link RetexturedHelper}
 */
@SuppressWarnings("WeakerAccess")
public abstract class RetexturedBlock extends Block implements EntityBlock {
  public RetexturedBlock(Properties properties) {
    super(properties);
  }

  @Override
  public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(world, pos, state, placer, stack);
    updateTextureBlock(world, pos, stack);
  }

  @Override
  public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state) {
    return getPickBlock(world, pos, state);
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    RetexturedHelper.addTooltip(stack, tooltip, flag);
  }


  /* Utils */

  /**
   * Call in {@link Block#setPlacedBy(Level, BlockPos, BlockState, LivingEntity, ItemStack)} to set the texture tag to the Tile Entity
   * @param world World where the block was placed
   * @param pos   Block position
   * @param stack Item stack
   */
  public static void updateTextureBlock(Level world, BlockPos pos, ItemStack stack) {
    if (stack.has(DataComponents.CUSTOM_DATA)) {
      BlockEntityHelper.get(IRetexturedBlockEntity.class, world, pos).ifPresent(te -> te.updateTexture(RetexturedHelper.getTextureName(stack)));
    }
  }

  /**
   * Called in blocks to get the item stack for the current block
   * @param world World
   * @param pos   Pos
   * @param state State
   * @return Pickblock stack with proper NBT
   */
  public static ItemStack getPickBlock(BlockGetter world, BlockPos pos, BlockState state) {
    Block block = state.getBlock();
    ItemStack stack = new ItemStack(block);
    BlockEntityHelper.get(IRetexturedBlockEntity.class, world, pos).ifPresent(te -> RetexturedHelper.setTexture(stack, te.getTextureName()));
    return stack;
  }
}
