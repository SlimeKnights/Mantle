package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.netty.handler.codec.EncoderException;
import lombok.RequiredArgsConstructor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

/** Loadable for an item stack */
@RequiredArgsConstructor
public enum ItemStackLoadable implements RecordLoadable<ItemStack> {
  /** Loads a non-empty item stack, ignoring NBT */
  NON_EMPTY(false, true),
  /** Loads a non-empty item stack, including NBT */
  NON_EMPTY_NBT(true, true),
  /** Loads an item stack that may be empty, ignoring NBT */
  EMPTY(false, false),
  /** Loads an item stack that may be empty, including NBT */
  EMPTY_NBT(true, false);

  /** If true, we read NBT */
  private final boolean readNBT;
  /** If true, we disallow reading empty stacks */
  private final boolean disallowEmpty;

  @Override
  public ItemStack convert(JsonElement element, String key) {
    if (!disallowEmpty && element.isJsonNull()) {
      return ItemStack.EMPTY;
    }
    if (element.isJsonPrimitive()) {
      Item item = Loadables.ITEM.convert(element, key);
      // air is empty, that may be disallowed
      if (disallowEmpty && item == Items.AIR) {
        throw new JsonSyntaxException("ItemStack at " + key + " may not be empty");
      }
      return new ItemStack(item);
    }
    return RecordLoadable.super.convert(element, key);
  }

  @Override
  public ItemStack getAndDeserialize(JsonObject parent, String key) {
    if (!disallowEmpty && !parent.has(key)) {
      return ItemStack.EMPTY;
    }
    return RecordLoadable.super.getAndDeserialize(parent, key);
  }

  /** Deserializes this stack from an object */
  @Override
  public ItemStack deserialize(JsonObject json) {
    Item item = Items.AIR;
    // if we disallow empty, force parsing the item so we get a missing field error
    // item field is optional if we allow empty
    if (json.has("item") || disallowEmpty) {
      item = Loadables.ITEM.getAndDeserialize(json, "item");
    }
    // air may come from the default or the registry, either is disallowed if we disallow empty
    if (item == Items.AIR) {
      if (disallowEmpty) {
        throw new JsonSyntaxException("ItemStack may not be empty");
      }
      return ItemStack.EMPTY;
    }
    // we handle empty via item, so count is not even considered, thus count of 0 is invalid
    int count = GsonHelper.getAsInt(json, "count", 1);
    if (count <= 0) {
      throw new JsonSyntaxException("ItemStack count must greater than 0");
    }
    ItemStack stack = new ItemStack(item, count);
    if (readNBT && json.has("nbt")) {
      stack.setTag(NBTLoadable.ALLOW_STRING.convert(json.get("nbt"), "nbt"));
    }
    return stack;
  }

  @Override
  public JsonElement serialize(ItemStack stack) {
    if (stack.isEmpty()) {
      if (disallowEmpty) {
        throw new IllegalArgumentException("FluidStack must not be empty");
      }
      return JsonNull.INSTANCE;
    }
    JsonElement item = Loadables.ITEM.serialize(stack.getItem());
    int count = stack.getCount();
    CompoundTag tag = readNBT ? stack.getTag() : null;
    if (count == 1 && tag == null) {
      return item;
    }
    JsonObject json = new JsonObject();
    json.add("item", item);
    json.addProperty("count", count);
    if (tag != null) {
      json.add("nbt", NBTLoadable.ALLOW_STRING.serialize(tag));
    }
    return json;
  }

  @Override
  public void serialize(ItemStack stack, JsonObject json) {
    if (stack.isEmpty()) {
      if (disallowEmpty) {
        throw new IllegalArgumentException("ItemStack must not be empty");
      }
      return;
    }
    json.add("item", Loadables.ITEM.serialize(stack.getItem()));
    json.addProperty("count", stack.getCount());
    CompoundTag tag = readNBT ? stack.getTag() : null;
    if (tag != null) {
      json.add("nbt", NBTLoadable.ALLOW_STRING.serialize(tag));
    }
  }

  @Override
  public ItemStack fromNetwork(FriendlyByteBuf buffer) {
    return buffer.readItem();
  }

  @Override
  public void toNetwork(ItemStack stack, FriendlyByteBuf buffer) throws EncoderException {
    buffer.writeItem(stack);
  }
}
