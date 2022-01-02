package slimeknights.mantle.client.book.data.element;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.repository.BookRepository;

import java.util.stream.Collectors;

public class ItemStackData implements IDataElement {

  public String itemList = null;
  public String tag = null;
  public transient String action;
  private transient NonNullList<ItemStack> items;

  public String id = "";
  public byte amount = 1;
  public JsonObject nbt;

  private transient boolean customData;
  private transient boolean isTag;
  private transient boolean tagLoaded;

  public NonNullList<ItemStack> getItems() {
    if (this.isTag && this.tagLoaded) {
      this.loadTag();
    }

    if (this.items != null) {
      return this.items;
    }

    return NonNullList.withSize(1, this.getItem());
  }

  private ItemStack getItem() {
    Item item;
    boolean isMissingItem = false;
    try {
      item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(this.id));
    } catch (Exception e) {
      item = Items.BARRIER;
      isMissingItem = true;
    }

    if (item == Items.AIR) {
      item = Items.BARRIER;
      isMissingItem = true;
    }

    ItemStack itemStack = new ItemStack(item, this.amount);

    if (this.nbt != null) {
      try {
        itemStack.setTag(JsonToNBT.getTagFromJson(filterJsonQuotes(this.nbt.toString())));
      } catch (CommandSyntaxException ignored) {
      }
    }

    if (isMissingItem) {
      CompoundNBT display = itemStack.getOrCreateChildTag("display");
      display.putString("Name", "\u00A7rUnknown Item");
      ListNBT lore = new ListNBT();
      lore.add(StringNBT.valueOf("\u00A7r\u00A7eItem Name:"));
      lore.add(StringNBT.valueOf("\u00A7r\u00A7e" + this.id));
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
    if (!ignoreNbt && stack.getTag() != null) {
      data.nbt = BookLoader.GSON.toJsonTree(stack.getTag(), CompoundNBT.class).getAsJsonObject();
    }

    return data;
  }

  public static ItemStackData getItemStackData(NonNullList<ItemStack> items) {
    ItemStackData data = new ItemStackData();
    data.items = items;
    data.customData = true;

    data.id = "->itemList";

    return data;
  }

  public static String filterJsonQuotes(String s) {
    return s.replaceAll("\"(\\w+)\"\\s*:", "$1: ");
  }

  private void loadTag() {
    this.isTag = true;
    this.tagLoaded = true;

    ITag<Item> values = ItemTags.getCollection().get(new ResourceLocation(this.tag));
    if (values != null) {
      this.items = values.getAllElements().stream().map(ItemStack::new).collect(Collectors.toCollection(NonNullList::create));
    } else {
      this.items = NonNullList.create();
    }
  }

  @Override
  public void load(BookRepository source) {
    if (this.customData) {
      return;
    }

    if (!StringUtils.isNullOrEmpty(this.tag) && ResourceLocation.isResouceNameValid(this.tag)) {
      this.loadTag();
      this.id = "->itemList";
      return;
    }

    ResourceLocation location = source.getResourceLocation(this.itemList);

    if (location != null) {
      this.id = "->itemList";

      if (source.resourceExists(location)) {
        try {
          ItemsList itemsList = BookLoader.GSON
            .fromJson(source.resourceToString(source.getResource(location)), ItemsList.class);
          this.items = NonNullList.withSize(itemsList.items.length, ItemStack.EMPTY);

          for (int i = 0; i < itemsList.items.length; i++) {
            this.items.set(i, itemsList.items[i].getItem());
          }

          this.action = itemsList.action;
        } catch (Exception ignored) {
        }
      }
    }
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
