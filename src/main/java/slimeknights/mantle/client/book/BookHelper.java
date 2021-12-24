package slimeknights.mantle.client.book;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.Objects;

public class BookHelper {

  public static final String BOOK_COMPOUND = "mantle";
  public static final String BOOK_DATA_COMPOUND = "book";

  public static final String NBT_CURRENT_PAGE = "current_page";

  /**
   * Returns the current saved page on the book
   * Returns an empty string is one is not found
   *
   * @param item The book to check for a saved page on
   * @return The current saved page
   */
  public static String getCurrentSavedPage(@Nullable ItemStack item) {
    if (item != null) {
      if (!item.isEmpty() && item.hasTag()) {
        CompoundTag bookNBT = item.getOrCreateTag().getCompound(BOOK_COMPOUND).getCompound(BOOK_DATA_COMPOUND);

        if (bookNBT.contains(NBT_CURRENT_PAGE, 8)) {
          return bookNBT.getString(NBT_CURRENT_PAGE);
        }
      }
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
    CompoundTag compoundNBT = stack.getOrCreateTag();

    CompoundTag mantleCompound = compoundNBT.getCompound(BOOK_COMPOUND);
    CompoundTag bookCompound = compoundNBT.getCompound(BOOK_DATA_COMPOUND);

    bookCompound.putString(NBT_CURRENT_PAGE, currentPage);

    mantleCompound.put(BOOK_DATA_COMPOUND, bookCompound);
    compoundNBT.put(BOOK_COMPOUND, mantleCompound);
    stack.setTag(compoundNBT);
  }
}
