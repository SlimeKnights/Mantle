package slimeknights.mantle.recipe.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.data.JsonCodec;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.common.ItemStackLoadable;
import slimeknights.mantle.util.JsonHelper;

import java.util.function.Supplier;

/**
 * Class representing an item stack output. Supports both direct stacks and tag output, behaving like an ingredient used for output
 */
public abstract class ItemOutput implements Supplier<ItemStack> {
  /** Codec instance */ // TODO: implement as a proper codec
  public static Codec<ItemOutput> CODEC = new JsonCodec<>() {
    @Override
    public ItemOutput deserialize(JsonElement element) {
      return ItemOutput.fromJson(element);
    }

    @Override
    public JsonElement serialize(ItemOutput output) {
      return output.serialize();
    }

    @Override
    public String toString() {
      return "ItemOutput";
    }
  };
  /** Loadable instance for an item output. Custom to handle the eithering behavior */
  public static Loadable<ItemOutput> LOADABLE = new Loadable<>() {
    @Override
    public ItemOutput convert(JsonElement element, String key) throws JsonSyntaxException {
      return ItemOutput.fromJson(element);
    }

    @Override
    public JsonElement serialize(ItemOutput object) throws RuntimeException {
      return object.serialize();
    }

    @Override
    public ItemOutput fromNetwork(FriendlyByteBuf buffer) throws DecoderException {
      return ItemOutput.read(buffer);
    }

    @Override
    public void toNetwork(ItemOutput object, FriendlyByteBuf buffer) throws EncoderException {
      object.write(buffer);
    }
  };


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
   * @param count Stack count
   * @return  Output
   */
  public static ItemOutput fromItem(ItemLike item, int count) {
    return new OfItem(item.asItem(), count);
  }

  /**
   * Creates a new output for the given item
   * @param item  Item
   * @return  Output
   */
  public static ItemOutput fromItem(ItemLike item) {
    return fromItem(item, 1);
  }

  /**
   * Creates a new output for the given tag
   * @param tag  Tag
   * @return Output
   */
  public static ItemOutput fromTag(TagKey<Item> tag, int count) {
    return new OfTagPreference(tag, count);
  }

  /**
   * Reads an item output from JSON
   * @param element  Json element
   * @return  Read output
   */
  public static ItemOutput fromJson(JsonElement element) {
    if (element.isJsonPrimitive()) {
      return fromItem(GsonHelper.convertToItem(element, "item"));
    }
    if (!element.isJsonObject()) {
      throw new JsonSyntaxException("Invalid item output, must be a string or an object");
    }
    // if it has a tag, parse as tag
    JsonObject json = element.getAsJsonObject();
    if (json.has("tag")) {
      TagKey<Item> tag = TagKey.create(Registry.ITEM_REGISTRY, JsonHelper.getResourceLocation(json, "tag"));
      int count = GsonHelper.getAsInt(json, "count", 1);
      return fromTag(tag, count);
    }

    // default: parse as item stack using loadables
    return fromStack(ItemStackLoadable.NON_EMPTY_NBT.convert(json, "item"));
  }

  /**
   * Writes this output to the packet buffer
   * @param buffer  Packet buffer instance
   */
  public void write(FriendlyByteBuf buffer) {
    buffer.writeItem(get());
  }

  /**
   * Reads an item output from the packet buffer
   * @param buffer  Buffer instance
   * @return  Item output
   */
  public static ItemOutput read(FriendlyByteBuf buffer) {
    return fromStack(buffer.readItem());
  }

  /** Class for an output that is just an item, simplifies NBT for serializing as vanilla forces NBT to be set for tools and forge goes through extra steps when NBT is set */
  @RequiredArgsConstructor
  private static class OfItem extends ItemOutput {
    private final Item item;
    private final int count;
    private ItemStack cachedStack;

    @Override
    public ItemStack get() {
      if (cachedStack == null) {
        cachedStack = new ItemStack(item, count);
      }
      return cachedStack;
    }

    @Override
    public JsonElement serialize() {
      String itemName = Registry.ITEM.getKey(item).toString();
      if (count > 1) {
        JsonObject json = new JsonObject();
        json.addProperty("item", itemName);
        json.addProperty("count", count);
        return json;
      } else {
        return new JsonPrimitive(itemName);
      }
    }
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
      return ItemStackLoadable.NON_EMPTY_NBT.serialize(stack);
    }
  }

  /** Class for an output from a tag preference */
  @RequiredArgsConstructor
  private static class OfTagPreference extends ItemOutput {
    private final TagKey<Item> tag;
    private final int count;
    private ItemStack cachedResult = null;

    @Override
    public ItemStack get() {
      // cache the result from the tag preference to save effort, especially helpful if the tag becomes invalid
      // this object should only exist in recipes so no need to invalidate the cache
      if (cachedResult == null) {
        cachedResult = TagPreference.getPreference(tag)
                                    .map(item -> new ItemStack(item, count))
                                    .orElse(ItemStack.EMPTY);
      }
      return cachedResult;
    }

    @Override
    public JsonElement serialize() {
      JsonObject json = new JsonObject();
      json.addProperty("tag", tag.location().toString());
      if (count != 1) {
        json.addProperty("count", count);
      }
      return json;
    }
  }
}
