package slimeknights.mantle.recipe.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.data.loadable.LoadableCodec;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.common.ItemStackLoadable;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Class representing an item stack output. Supports both direct stacks and tag output, behaving like an ingredient used for output
 */
public abstract class ItemOutput implements Supplier<ItemStack> {
  /* Codecs - just adding these as needed */
  /** Codec for an output that may not be empty with any size */
  public static Codec<ItemOutput> REQUIRED_STACK_CODEC = new LoadableCodec<>(Loadable.REQUIRED_STACK);

  /** Empty instance */
  public static final ItemOutput EMPTY = new OfStack(ItemStack.EMPTY);


  /**
   * Gets the item output of this recipe
   * @return  Item output
   */
  @Override
  public abstract ItemStack get();

  /**
   * Writes this output to JSON
   * @param  writeCount  If true, serializes the count
   * @return  Json element
   */
  public abstract JsonElement serialize(boolean writeCount);

  /**
   * Creates a new output for the given stack
   * @param stack  Stack
   * @return  Output
   */
  public static ItemOutput fromStack(ItemStack stack) {
    if (stack.isEmpty()) {
      return EMPTY;
    }
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
   * @param tag   Tag
   * @param count Stack count
   * @return Output
   */
  public static ItemOutput fromTag(TagKey<Item> tag, int count) {
    return new OfTagPreference(tag, count);
  }

  /**
   * Creates a new output for the given tag
   * @param tag  Tag
   * @return Output
   */
  public static ItemOutput fromTag(TagKey<Item> tag) {
    return fromTag(tag, 1);
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
    public JsonElement serialize(boolean writeCount) {
      JsonElement item = Loadables.ITEM.serialize(this.item);
      if (writeCount && count > 1) {
        JsonObject json = new JsonObject();
        json.add("item", item);
        json.addProperty("count", count);
        return json;
      } else {
        return item;
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
    public JsonElement serialize(boolean writeCount) {
      if (writeCount) {
        return ItemStackLoadable.OPTIONAL_STACK_NBT.serialize(stack);
      }
      return ItemStackLoadable.OPTIONAL_ITEM_NBT.serialize(stack);
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
        // if the preference is empty, do not cache it.
        // This should only happen if someone scans recipes before tag are computed in which case we cache the wrong resolt.
        // We protect against empty tags in our recipes via conditions.
        Optional<Item> preference = TagPreference.getPreference(tag);
        if (preference.isEmpty()) {
          return ItemStack.EMPTY;
        }
        cachedResult = new ItemStack(preference.orElseThrow(), count);
      }
      return cachedResult;
    }

    @Override
    public JsonElement serialize(boolean writeCount) {
      JsonObject json = new JsonObject();
      json.addProperty("tag", tag.location().toString());
      if (writeCount) {
        json.addProperty("count", count);
      }
      return json;
    }
  }

  /** Loadable logic for an ItemOutput */
  public enum Loadable implements slimeknights.mantle.data.loadable.Loadable<ItemOutput> {
    /** Loadable for an output that may be empty with a fixed size of 1 */
    OPTIONAL_ITEM(false, false),
    /** Loadable for an output that may be empty with any size */
    OPTIONAL_STACK(false, true),
    /** Loadable for an output that may not empty with a fixed size of 1 */
    REQUIRED_ITEM(true, false),
    /** Loadable for an output that may not be empty with any size */
    REQUIRED_STACK(true, true);

    private final boolean nonEmpty;
    private final boolean readCount;
    private final RecordLoadable<ItemStack> stack;
    Loadable(boolean nonEmpty, boolean readCount) {
      this.nonEmpty = nonEmpty;
      this.readCount = readCount;
      // figure out the stack serializer to use based on the two parameters
      // we always do NBT, just those that vary
      if (nonEmpty) {
        this.stack = readCount ? ItemStackLoadable.REQUIRED_STACK_NBT : ItemStackLoadable.REQUIRED_ITEM_NBT;
      } else {
        this.stack = readCount ? ItemStackLoadable.OPTIONAL_STACK_NBT : ItemStackLoadable.OPTIONAL_ITEM_NBT;
      }
    }

    @Override
    public ItemOutput convert(JsonElement element, String key) {
      // if it's a primitive, parse it directly with the stack logic
      // that handles single items and ensures both count and non-empty
      if (element.isJsonPrimitive()) {
        return fromStack(stack.convert(element, key));
      }
      JsonObject json = GsonHelper.convertToJsonObject(element, key);
      if (json.has("tag")) {
        TagKey<Item> tag = Loadables.ITEM_TAG.getIfPresent(json, "tag");
        int count = 1;
        // 0 count field means we load count from JSON
        if (readCount) {
          count = IntLoadable.FROM_ONE.getOrDefault(json, "count", 1);
        }
        return fromTag(tag, count);
      }
      return fromStack(stack.deserialize(json));
    }

    @Override
    public JsonElement serialize(ItemOutput output) {
      if (nonEmpty && (output instanceof OfItem || output instanceof OfStack) && output.get().isEmpty()) {
        throw new IllegalArgumentException("ItemOutput cannot be empty for this recipe");
      }
      return output.serialize(readCount);
    }

    @Override
    public ItemOutput decode(FriendlyByteBuf buffer) {
      return fromStack(stack.decode(buffer));
    }

    @Override
    public void encode(FriendlyByteBuf buffer, ItemOutput object) {
      stack.encode(buffer, object.get());
    }


    /* Defaulting behavior */

    /** Gets the output, defaulting to empty. Note this will not stop you from getting empty with a non-empty loadable, thats on you for weirdly calling. */
    public ItemOutput getOrEmpty(JsonObject parent, String key) {
      return getOrDefault(parent, key, ItemOutput.EMPTY);
    }

    /** Creates a field defaulting to empty */
    public <P> LoadableField<ItemOutput,P> emptyField(String key, boolean serializeDefault, Function<P,ItemOutput> getter) {
      return defaultField(key, ItemOutput.EMPTY, serializeDefault, getter);
    }

    /** Creates a field defaulting to empty that does not serialize if empty */
    public <P> LoadableField<ItemOutput,P> emptyField(String key, Function<P,ItemOutput> getter) {
      return emptyField(key, false, getter);
    }
  }
}
