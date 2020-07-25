package slimeknights.mantle.client.book;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.Objects;

public class BookHelper {

  public static String getSavedPage(ItemStack item) {
    if (!item.isEmpty() && item.hasTag()) {
      CompoundNBT mantleBook = Objects.requireNonNull(item.getTag()).getCompound("mantle").getCompound("book");

      if (mantleBook.contains("page", 8)) {
        return mantleBook.getString("page");
      }
    }

    return "";
  }

  public static void writeSavedPage(ItemStack item, String page) {
    CompoundNBT compound = item.getTag();

    if (compound == null) {
      compound = new CompoundNBT();
    }

    CompoundNBT mantle = compound.getCompound("mantle");
    CompoundNBT book = mantle.getCompound("book");

    book.putString("page", page);

    mantle.put("book", book);
    compound.put("mantle", mantle);
    item.setTag(compound);
  }

}
