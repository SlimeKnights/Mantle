package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.function.BiFunction;
import java.util.function.Function;

import static slimeknights.mantle.util.DataComponentHelper.makeStack;

/** Loadable for an item stack */
@SuppressWarnings("unused")  // API
public class ItemStackLoadable {
  private ItemStackLoadable() {}

  /* reused lambdas */
  /** Getter for an item from a stack */
  private static final Function<ItemStack,Item> ITEM_GETTER = ItemStack::getItem;
  /** Maps an item stack that may be empty to a strictly not empty one */
  private static final BiFunction<ItemStack,ErrorFactory,ItemStack> NOT_EMPTY = (stack, error) -> {
    if (stack.isEmpty()) {
      throw error.create("ItemStack cannot be empty");
    }
    return stack;
  };

  /* fields */
  /** Field for an optional item */
  private static final LoadableField<Item,ItemStack> ITEM = Loadables.ITEM.defaultField("item", Items.AIR, false, ITEM_GETTER);
  /** Field for item stack count that allows empty */
  private static final LoadableField<Integer,ItemStack> COUNT = IntLoadable.FROM_ZERO.defaultField("count", 1, true, ItemStack::getCount);
  /** Field for item stack count that allows empty */
  private static final LoadableField<DataComponentPatch,ItemStack> COMPONENTS = Loadables.DATA_COMPONENTS.defaultField("components", DataComponentPatch.EMPTY, false, ItemStack::getComponentsPatch);


  /* Optional */
  /** Single item which may be empty with a count of 1 */
  public static final Loadable<ItemStack> OPTIONAL_ITEM = Loadables.ITEM.flatXmap(item -> makeStack(item, 1, DataComponentPatch.EMPTY), ITEM_GETTER);
  /** Loadable for a stack that may be empty with variable count */
  public static final RecordLoadable<ItemStack> OPTIONAL_STACK = RecordLoadable.create(ITEM, COUNT, (item, count) -> makeStack(item, count, DataComponentPatch.EMPTY))
                                                                               .compact(OPTIONAL_ITEM, stack -> stack.getCount() == 1);
  /** Loadable for a stack that may be empty with components and a count of 1 */
  public static final RecordLoadable<ItemStack> OPTIONAL_ITEM_DATA = DataComponentsStack.FIXED_COUNT;
  /** Loadable for a stack that may be empty with variable count and components */
  public static final RecordLoadable<ItemStack> OPTIONAL_STACK_DATA = DataComponentsStack.READ_COUNT;

  /* Required */
  /** Single item which may not be empty with a count of 1 */
  public static final Loadable<ItemStack> REQUIRED_ITEM = notEmpty(OPTIONAL_ITEM);
  /** Loadable for a stack that may not be empty with variable count */
  public static final RecordLoadable<ItemStack> REQUIRED_STACK = notEmpty(OPTIONAL_STACK);
  /** Loadable for a stack that may not be empty with components and a count of 1 */
  public static final RecordLoadable<ItemStack> REQUIRED_ITEM_COMPONENTS = notEmpty(OPTIONAL_ITEM_DATA);
  /** Loadable for a stack that may not be empty with variable count and components */
  public static final RecordLoadable<ItemStack> REQUIRED_STACK_COMPONENTS = notEmpty(OPTIONAL_STACK_DATA);


  /* Helpers */

  /** Creates a non-empty variant of the loadable */
  public static Loadable<ItemStack> notEmpty(Loadable<ItemStack> loadable) {
    return loadable.validate(NOT_EMPTY);
  }

  /** Creates a non-empty variant of the loadable */
  public static RecordLoadable<ItemStack> notEmpty(RecordLoadable<ItemStack> loadable) {
    return loadable.validate(NOT_EMPTY);
  }

  /** Loadable for an item stack with NBT, requires special logic due to forges share tags */
  private enum DataComponentsStack implements RecordLoadable<ItemStack> {
    /** Reads count from JSON */
    READ_COUNT,
    /** Count is always 1 */
    FIXED_COUNT;


    /* General JSON */

    @Override
    public ItemStack deserialize(JsonObject json, TypedMap context) {
      int count = 1;
      if (this == READ_COUNT) {
        count = COUNT.get(json, context);
      }
      return makeStack(ITEM.get(json, context), count, COMPONENTS.get(json, context));
    }

    @Override
    public void serializeInto(ItemStack stack, JsonObject json, TypedMap context) {
      ITEM.serializeInto(stack, json, context);
      if (this == READ_COUNT) {
        COUNT.serializeInto(stack, json, context);
      }
      COMPONENTS.serializeInto(stack, json, context);
    }


    /* Compact JSON */

    @Override
    public ItemStack convert(JsonElement element, String key, TypedMap context) {
      if (element.isJsonPrimitive()) {
        return OPTIONAL_ITEM.convert(element, key, context);
      }
      return RecordLoadable.super.convert(element, key, context);
    }

    @Override
    public JsonElement serialize(ItemStack stack, TypedMap context) {
      if ((this == FIXED_COUNT || stack.getCount() == 1) && !stack.getComponentsPatch().isEmpty()) {
        return OPTIONAL_ITEM.serialize(stack, context);
      }
      return RecordLoadable.super.serialize(stack, context);
    }


    /* Buffer */

    @Override
    public ItemStack decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
      // not using makeItemStack as we need to set the share tag NBT here
      Item item = ITEM.decode(buffer, context);
      int count = 1;
      if (this == READ_COUNT) {
        count = COUNT.decode(buffer, context);
      }
      return makeStack(item, count, COMPONENTS.decode(buffer, context));
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer, ItemStack stack, TypedMap context) throws EncoderException {
      ITEM.encode(buffer, stack, context);
      if (this == READ_COUNT) {
        COUNT.encode(buffer, stack, context);
      }
      COMPONENTS.encode(buffer, stack, context);
    }
  }
}
