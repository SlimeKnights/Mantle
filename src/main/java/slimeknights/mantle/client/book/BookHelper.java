package slimeknights.mantle.client.book;

import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.registration.MantleDataComponents;

import javax.annotation.Nullable;

/** Helpers for working with book items */
public class BookHelper {
  /**
   * Returns the current saved page on the book
   * Returns an empty string is one is not found
   *
   * @param item The book to check for a saved page on
   * @return The current saved page
   */
  public static String getCurrentSavedPage(@Nullable ItemStack item) {
    if (item != null) {
      return item.getOrDefault(MantleDataComponents.BOOK_PAGE, "");
    }
    return "";
  }

  /**
   * Saves the current open page to the given book ItemStack.
   *
   * @param stack       the current book stack
   * @param currentPage the current open page
   */
  public static void writeSavedPageToBook(ItemStack stack, String currentPage) {
    stack.set(MantleDataComponents.BOOK_PAGE, currentPage);
  }
}
