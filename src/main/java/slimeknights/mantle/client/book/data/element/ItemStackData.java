package slimeknights.mantle.client.book.data.element;

import com.google.gson.JsonObject;
import net.minecraft.command.CommandGive;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import slimeknights.mantle.client.book.BookLoader;

public class ItemStackData {

  public String id = "";
  public byte amount = 1;
  public short damage = 0;
  public JsonObject nbt;

  public ItemStack getItemStack() {
    Item item;
    boolean isMissingItem = false;
    try {
      item = CommandGive.getItemByText(null, id);
    } catch (NumberInvalidException e) {
      item = Item.getItemFromBlock(Blocks.barrier);
      isMissingItem = true;
    }

    if (item == null) {
      item = Item.getItemFromBlock(Blocks.barrier);
      isMissingItem = true;
    }

    ItemStack itemStack = new ItemStack(item, amount, damage);

    if (nbt != null) {
      try {
        itemStack.setTagCompound(JsonToNBT.getTagFromJson(nbt.toString()));
      } catch (NBTException ignored) {
      }
    }

    if (isMissingItem) {
      NBTTagCompound display = itemStack.getSubCompound("display", true);
      display.setString("Name", "\u00A7rUnknown Item");
      NBTTagList lore = new NBTTagList();
      lore.appendTag(new NBTTagString("\u00A7r\u00A7eItem Name:"));
      lore.appendTag(new NBTTagString("\u00A7r\u00A7e" + id));
      display.setTag("Lore", lore);
    }

    return itemStack;
  }

  public static ItemStackData getItemStackData(ItemStack stack) {
    return getItemStackData(stack, false);
  }

  public static ItemStackData getItemStackData(ItemStack stack, boolean ignoreNbt) {
    ItemStackData data = new ItemStackData();
    data.id = Item.itemRegistry.getNameForObject(stack.getItem()).toString();
    data.amount = (byte) stack.stackSize;
    data.damage = (short) stack.getItemDamage();
    if (!ignoreNbt && stack.getTagCompound() != null) {
      data.nbt = BookLoader.GSON.toJsonTree(stack.getTagCompound(), NBTTagCompound.class).getAsJsonObject();
    }

    return data;
  }

  public static class ItemLink {

    public ItemStackData item = new ItemStackData();
    public boolean damageSensitive = false;
    public String action = "";
  }
}
