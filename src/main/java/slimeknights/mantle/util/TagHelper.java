package slimeknights.mantle.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

public class TagHelper {

  public static int TAG_TYPE_STRING = (new NBTTagString()).getId();
  public static int TAG_TYPE_COMPOUND = (new NBTTagCompound()).getId();

  private TagHelper() {
  }

  /* Generic Tag Operations */
  public static NBTTagCompound getTagSafe(ItemStack stack) {
    if(stack.isEmpty() || !stack.hasTag()) {
      return new NBTTagCompound();
    }

    return stack.getTag();
  }

  public static NBTTagCompound getTagSafe(NBTTagCompound tag, String key) {
    if(tag == null || !tag.contains(key)) {
      return new NBTTagCompound();
    }

    return tag.getCompound(key);
  }

  public static NBTTagList getTagListSafe(NBTTagCompound tag, String key, int type) {
    if(tag == null || !tag.contains(key)) {
      return new NBTTagList();
    }

    return tag.getList(key, type);
  }

}
