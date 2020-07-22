package slimeknights.mantle.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TagHelper {
  /**
   * Gets the tag from a stack, or an empty tag if the stack has none
   * @param stack  Stack to fetch tag
   * @return  Stack tag or an empty tag
   */
  @SuppressWarnings("ConstantConditions")
  public static CompoundNBT getTagSafe(ItemStack stack) {
    if (stack.isEmpty() || !stack.hasTag()) {
      return new CompoundNBT();
    }

    return stack.getTag();
  }

  /**
   * Gets the value from the given tag
   * @param tag  Parent tag
   * @param key  Key to get
   * @return  Compound at the key, or empty tag if no value
   */
  public static CompoundNBT getTagSafe(@Nullable CompoundNBT tag, String key) {
    if (tag == null || !tag.contains(key, NBT.TAG_COMPOUND)) {
      return new CompoundNBT();
    }
    return tag.getCompound(key);
  }

  /**
   * Gets a tag list from an object, or an empty list if its missing
   * @param tag   Parent tag
   * @param key   List key
   * @param type  List type
   * @return  List at key, or empty list if missing
   */
  public static ListNBT getTagListSafe(@Nullable CompoundNBT tag, String key, int type) {
    if (tag == null || !tag.contains(key, NBT.TAG_LIST)) {
      return new ListNBT();
    }
    return tag.getList(key, type);
  }
}
