package slimeknights.mantle.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import lombok.RequiredArgsConstructor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraftforge.common.crafting.CraftingHelper;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Class representing an item stack output. Supports both direct stacks and tag output, behaving like an ingredient used for output
 */
public abstract class ItemOutput implements Supplier<ItemStack> {
  /**
   * Gets the item output of this recipe
   * @return  Item output
   */
  @Override
  public abstract ItemStack get();

  /**
   * Writes this output to JSON
   * @return  Json element
   */
  public abstract JsonElement serialize();

  /**
   * Creates a new output for the given stack
   * @param stack  Stack
   * @return  Output
   */
  public static ItemOutput fromStack(ItemStack stack) {
    return new OfStack(stack);
  }

  /**
   * Creates a new output for the given item
   * @param item  Item
   * @return  Output
   */
  public static ItemOutput fromItem(ItemConvertible item) {
    return fromStack(new ItemStack(item));
  }

  /**
   * Creates a new output for the given tag
   * @param tag  Tag
   * @return Output
   */
  public static ItemOutput fromTag(Tag<Item> tag, int count) {
    return new OfTagPreference(tag, count);
  }

  /**
   * Reads an item output from JSON
   * @param element  Json element
   * @return  Read output
   */
  public static ItemOutput fromJson(JsonElement element) {
    if (element.isJsonPrimitive()) {
      return fromItem(JsonHelper.asItem(element, "item"));
    }
    if (!element.isJsonObject()) {
      throw new JsonSyntaxException("Invalid item output, must be a string or an object");
    }
    // if it has a tag, parse as tag
    JsonObject json = element.getAsJsonObject();
    if (json.has("tag")) {
      String name = JsonHelper.getString(json, "tag");
      Tag<Item> tag = ServerTagManagerHolder.getTagManager().getItems().getTag(new Identifier(name));
      if (tag == null) {
        throw new JsonSyntaxException("Unknown tag " + name + " for item output");
      }
      int count = JsonHelper.getInt(json, "count", 1);
      return fromTag(tag, count);
    }

    // default: parse as item stack using Forge
    return fromStack(CraftingHelper.getItemStack(json, true));
  }

  /**
   * Writes this output to the packet buffer
   * @param buffer  Packet buffer instance
   */
  public void write(PacketByteBuf buffer) {
    buffer.writeItemStack(get());
  }

  /**
   * Reads an item output from the packet buffer
   * @param buffer  Buffer instance
   * @return  Item output
   */
  public static ItemOutput read(PacketByteBuf buffer) {
    return fromStack(buffer.readItemStack());
  }

  /** Class for an output that is just a stack */
  @RequiredArgsConstructor
  private static class OfStack extends ItemOutput {
    private final ItemStack stack;

    @Override
    public ItemStack get() {
      return stack;
    }

    @Override
    public JsonElement serialize() {
      String itemName = Objects.requireNonNull(stack.getItem().getRegistryName()).toString();
      int count = stack.getCount();
      // if the item has NBT or a count, write as object
      if (stack.hasTag() || stack.getCount() > 1) {
        JsonObject jsonResult = new JsonObject();
        jsonResult.addProperty("item", itemName);
        if (count > 1) {
          jsonResult.addProperty("count", count);
        }
        CompoundTag nbt = stack.getTag();
        if (nbt != null) {
          jsonResult.addProperty("nbt", nbt.toString());
        }
        return jsonResult;
      } else {
        return new JsonPrimitive(itemName);
      }
    }
  }

  /** Class for an output from a tag preference */
  @RequiredArgsConstructor
  private static class OfTagPreference extends ItemOutput {
    private final Tag<Item> tag;
    private final int count;

    @Override
    public ItemStack get() {
      return TagPreference.getItems().getPreference(tag)
                          .map(item -> new ItemStack(item, count))
                          .orElse(ItemStack.EMPTY);
    }

    @Override
    public JsonElement serialize() {
      JsonObject json = new JsonObject();
      json.addProperty("tag", ServerTagManagerHolder.getTagManager().getItems().getTagId(tag).toString());
      if (count != 1) {
        json.addProperty("count", count);
      }
      return json;
    }
  }
}
