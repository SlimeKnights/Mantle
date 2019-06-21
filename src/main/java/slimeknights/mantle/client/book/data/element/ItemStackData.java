package slimeknights.mantle.client.book.data.element;

import com.google.gson.JsonObject;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.repository.BookRepository;

public class ItemStackData {

  public String itemList = null;
  public transient BookRepository source;
  public transient ResourceLocation itemListLocation = null;
  public transient String action;

  public String id = "";
  public byte amount = 1;
  public JsonObject nbt;

  public NonNullList<ItemStack> getItems() {
    if(itemListLocation != null && source.resourceExists(itemListLocation)) {
      try {
        ItemsList itemsList = BookLoader.GSON
            .fromJson(source.resourceToString(source.getResource(itemListLocation)), ItemsList.class);
        NonNullList<ItemStack> items = NonNullList.<ItemStack> withSize(itemsList.items.length, ItemStack.EMPTY);

        for(int i = 0; i < itemsList.items.length; i++) {
          items.set(i, itemsList.items[i].getItem());
        }

        this.action = itemsList.action;

        return items;
      } catch(Exception ignored) {
      }
    }

    return NonNullList.<ItemStack> withSize(1, getItem());
  }

  private ItemStack getItem() {
    Item item;
    boolean isMissingItem = false;
    try {
      item = ItemArgument.getItem(null, id).getItem();
    } catch(Exception e) {
      item = Item.getItemFromBlock(Blocks.BARRIER);
      isMissingItem = true;
    }

    if(item == Items.AIR) {
      item = Item.getItemFromBlock(Blocks.BARRIER);
      isMissingItem = true;
    }

    ItemStack itemStack = new ItemStack(item, amount);

    if(nbt != null) {
      try {
        itemStack.setTag(JsonToNBT.getTagFromJson(filterJsonQuotes(nbt.toString())));
      } catch(CommandSyntaxException ignored) {
      }
    }

    if(isMissingItem) {
      CompoundNBT display = itemStack.getOrCreateChildTag("display");
      display.putString("Name", "\u00A7rUnknown Item");
      ListNBT lore = new ListNBT();
      lore.add(new StringNBT("\u00A7r\u00A7eItem Name:"));
      lore.add(new StringNBT("\u00A7r\u00A7e" + id));
      display.put("Lore", lore);
    }

    return itemStack;
  }

  public static ItemStackData getItemStackData(ItemStack stack) {
    return getItemStackData(stack, false);
  }

  public static ItemStackData getItemStackData(ItemStack stack, boolean ignoreNbt) {
    ItemStackData data = new ItemStackData();
    data.id = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
    data.amount = (byte) stack.getCount();
    if(!ignoreNbt && stack.getTag() != null) {
      data.nbt = BookLoader.GSON.toJsonTree(stack.getTag(), CompoundNBT.class).getAsJsonObject();
    }

    return data;
  }

  public static String filterJsonQuotes(String s) {
    return s.replaceAll("\"(\\w+)\"\\s*:", "$1: ");
  }

  private static class ItemsList {

    public ItemStackData[] items = new ItemStackData[0];
    public String action;
  }

  public static class ItemLink {

    public ItemStackData item = new ItemStackData();
    public boolean damageSensitive = false;
    public String action = "";
  }
}
