package slimeknights.mantle.client.book;

import java.util.Arrays;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class BookHelper {

  public static String getSavedPage(ItemStack item) {
    if (item != null && item.hasTagCompound()) {
      NBTTagCompound mantleBook = item.getTagCompound().getCompoundTag("mantle").getCompoundTag("book");

      if (mantleBook.hasKey("page", Arrays.asList(NBTBase.NBT_TYPES).indexOf("STRING"))) {
        return mantleBook.getString("page");
      }
    }

    return "";
  }

  public static void writeSavedPage(ItemStack item, String page) {
    NBTTagCompound compound = item.getTagCompound();

    if (compound == null)
      compound = new NBTTagCompound();

    NBTTagCompound mantle = compound.getCompoundTag("mantle");
    NBTTagCompound book = mantle.getCompoundTag("book");

    book.setString("page", page);

    mantle.setTag("book", book);
    compound.setTag("mantle", mantle);
    item.setTagCompound(compound);
  }

}
