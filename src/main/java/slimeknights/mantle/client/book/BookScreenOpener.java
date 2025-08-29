package slimeknights.mantle.client.book;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

/**
 * This interface allows calling methods from {@link slimeknights.mantle.client.book.data.BookData} without class loading client classes.
 * Thus, despite being in the client package, this is safe to use on the serverside.
 * @apiNote This interface should not be implemented, just use {@link slimeknights.mantle.client.book.data.BookData}
 */
@NonExtendable
public interface BookScreenOpener {
  /**
   * Opens the GUI for a held book
   * @param hand   Hand containing the book
   * @param stack  Book stack
   */
  void openGui(InteractionHand hand, ItemStack stack);

  /**
   * Opens the GUI for a held book
   * @param slot   Slot containing the book
   * @param stack  Book stack
   */
  void openGui(int slot, ItemStack stack);

  /**
   * Opens the GUI for a lectern containing the book
   * @param pos    Position of the lectern
   * @param stack  Item in the lectern
   */
  void openGui(BlockPos pos, ItemStack stack);
}
