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
    if(stack.isEmpty() || stack.getItem() == null || !stack.hasTagCompound()) {
      return new NBTTagCompound();
    }

    return stack.getTagCompound();
  }

  public static NBTTagCompound getTagSafe(NBTTagCompound tag, String key) {
    if(tag == null || !tag.hasKey(key)) {
      return new NBTTagCompound();
    }

    return tag.getCompoundTag(key);
  }

  public static NBTTagList getTagListSafe(NBTTagCompound tag, String key, int type) {
    if(tag == null || !tag.hasKey(key)) {
      return new NBTTagList();
    }

    return tag.getTagList(key, type);
  }

}
