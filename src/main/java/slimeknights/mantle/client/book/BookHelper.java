package slimeknights.mantle.client.book;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Arrays;

public class BookHelper {

  public static String getSavedPage(ItemStack item) {
    if(!item.isEmpty() && item.hasTag()) {
      NBTTagCompound mantleBook = item.getTag().getCompound("mantle").getCompound("book");

      if(mantleBook.contains("page", Arrays.asList(INBTBase.NBT_TYPES).indexOf("STRING"))) {
        return mantleBook.getString("page");
      }
    }

    return "";
  }

  public static void writeSavedPage(ItemStack item, String page) {
    NBTTagCompound compound = item.getTag();

    if(compound == null) {
      compound = new NBTTagCompound();
    }

    NBTTagCompound mantle = compound.getCompound("mantle");
    NBTTagCompound book = mantle.getCompound("book");

    book.putString("page", page);

    mantle.put("book", book);
    compound.put("mantle", mantle);
    item.setTag(compound);
  }

}
