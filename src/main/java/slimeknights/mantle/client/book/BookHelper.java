package slimeknights.mantle.client.book;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

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
    if (item != null && !item.isEmpty()) {
      CustomData customData = item.get(DataComponents.CUSTOM_DATA);
      if (customData != null) {
        CompoundTag tag = customData.copyTag();
        CompoundTag bookNBT = tag.getCompound(BOOK_COMPOUND).getCompound(BOOK_DATA_COMPOUND);
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
    stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> {
      CompoundTag tag = data.copyTag();
      CompoundTag mantleCompound = tag.getCompound(BOOK_COMPOUND);
      CompoundTag bookCompound = mantleCompound.getCompound(BOOK_DATA_COMPOUND);
      
      bookCompound.putString(NBT_CURRENT_PAGE, currentPage);
      mantleCompound.put(BOOK_DATA_COMPOUND, bookCompound);
      tag.put(BOOK_COMPOUND, mantleCompound);
      
      return CustomData.of(tag);
    });
  }
}
