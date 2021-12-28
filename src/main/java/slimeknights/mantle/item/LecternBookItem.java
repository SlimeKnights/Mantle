package slimeknights.mantle.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import slimeknights.mantle.util.BlockEntityHelper;

/**
 * Book item that can be placed on lecterns
 */
public abstract class LecternBookItem extends TooltipItem implements ILecternBookItem {
  public LecternBookItem(Properties properties) {
    super(properties);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    Level level = context.getLevel();
    BlockPos pos = context.getClickedPos();
    BlockState state = level.getBlockState(pos);
    if (state.is(Blocks.LECTERN)) {
      if (LecternBlock.tryPlaceBook(context.getPlayer(), level, pos, state, context.getItemInHand())) {
        return InteractionResult.sidedSuccess(level.isClientSide);
      }
    }
    return InteractionResult.PASS;
  }

  /**
   * Event handler to control the lectern GUI
   */
  public static void interactWithBlock(PlayerInteractEvent.RightClickBlock event) {
    Level world = event.getWorld();
    // client side has no access to the book, so just skip
    if (world.isClientSide() || event.getPlayer().isShiftKeyDown()) {
      return;
    }
    // must be a lectern, and have the TE
    BlockPos pos = event.getPos();
    BlockState state = world.getBlockState(pos);
    if (state.is(Blocks.LECTERN)) {
      BlockEntityHelper.get(LecternBlockEntity.class, world, pos)
											 .ifPresent(te -> {
                        ItemStack book = te.getBook();
                        if (!book.isEmpty() && book.getItem() instanceof ILecternBookItem
                            && ((ILecternBookItem) book.getItem()).openLecternScreen(world, pos, event.getPlayer(), book)) {
                          event.setCanceled(true);
                        }
                      });
    }
  }

}
