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
import slimeknights.mantle.client.book.BookLoader;

public class ItemStackData {

  public String id = "minecraft:barrier";
  public byte amount = 1;
  public short damage = 0;
  public JsonObject nbt;

  public ItemStack getItemStack() {
    Item item;
    try {
      item = CommandGive.getItemByText(null, id);
    } catch (NumberInvalidException e) {
      item = Item.getItemFromBlock(Blocks.barrier);
    }

    if (item == null)
      item = Item.getItemFromBlock(Blocks.barrier);

    ItemStack itemStack = new ItemStack(item, amount, damage);

    if (nbt != null) {
      try {
        itemStack.setTagCompound(JsonToNBT.getTagFromJson(nbt.toString()));
      } catch (NBTException ignored) {
      }
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
