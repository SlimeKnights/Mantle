package slimeknights.mantle.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;

public class TagHelper {

  public static int TAG_TYPE_STRING = (StringNBT.func_229705_a_("")).getId();
  public static int TAG_TYPE_COMPOUND = (new CompoundNBT()).getId();

  private TagHelper() {
  }

  /* Generic Tag Operations */
  public static CompoundNBT getTagSafe(ItemStack stack) {
    if (stack.isEmpty() || !stack.hasTag()) {
      return new CompoundNBT();
    }

    return stack.getTag();
  }

  public static CompoundNBT getTagSafe(CompoundNBT tag, String key) {
    if (tag == null || !tag.contains(key)) {
      return new CompoundNBT();
    }

    return tag.getCompound(key);
  }

  public static ListNBT getTagListSafe(CompoundNBT tag, String key, int type) {
    if (tag == null || !tag.contains(key)) {
      return new ListNBT();
    }

    return tag.getList(key, type);
  }

}
