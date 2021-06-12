package slimeknights.mantle.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import slimeknights.mantle.network.MantleNetwork;
import slimeknights.mantle.network.packet.OpenLecternBookPacket;

/** Interface for book items to work with lecterns */
public interface ILecternBookItem {
  /**
   * Called serverside to open the lectern screen when a lectern is clicked
   * @param world    World
   * @param pos      Block position
   * @param player   Player instance
   * @param book     Book stack
   * @return  True if the normal screen should not be opened
   */
  default boolean openLecternScreen(World world, BlockPos pos, PlayerEntity player, ItemStack book) {
    MantleNetwork.INSTANCE.sendTo(new OpenLecternBookPacket(pos, book), player);
    return true;
  }

  /**
   * Called client side to open the lectern screen, unsafe to call serverside.
   * Typical implementions will make use of {@link slimeknights.mantle.client.book.data.BookData#openGui(BlockPos, ItemStack)}
   * @param pos   Lectern position
   * @param book  Book stack instance
   */
  void openLecternScreenClient(BlockPos pos, ItemStack book);
}
