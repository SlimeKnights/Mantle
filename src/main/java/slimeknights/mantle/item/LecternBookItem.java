package slimeknights.mantle.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.LecternTileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import slimeknights.mantle.util.TileEntityHelper;

/**
 * Book item that can be placed on lecterns
 */
public abstract class LecternBookItem extends TooltipItem implements ILecternBookItem {
  public LecternBookItem(Properties properties) {
    super(properties);
  }

  @Override
  public ActionResultType onItemUse(ItemUseContext context) {
    World world = context.getWorld();
    BlockPos pos = context.getPos();
    BlockState state = world.getBlockState(pos);
    if (state.isIn(Blocks.LECTERN)) {
      if (LecternBlock.tryPlaceBook(world, pos, state, context.getItem())) {
        return ActionResultType.func_233537_a_(world.isRemote);
      }
    }
    return ActionResultType.PASS;
  }

  /**
   * Event handler to control the lectern GUI
   */
  public static void interactWithBlock(PlayerInteractEvent.RightClickBlock event) {
    World world = event.getWorld();
    // client side has no access to the book, so just skip
    if (world.isRemote() || event.getPlayer().isSneaking()) {
      return;
    }
    // must be a lectern, and have the TE
    BlockPos pos = event.getPos();
    BlockState state = world.getBlockState(pos);
    if (state.isIn(Blocks.LECTERN)) {
      TileEntityHelper.getTile(LecternTileEntity.class, world, pos)
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
