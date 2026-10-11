package slimeknights.mantle.recipe.helper;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import slimeknights.mantle.data.codec.MantleCodecs;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.Streamable;
import slimeknights.mantle.data.loadable.common.CodecStreamable;
import slimeknights.mantle.data.loadable.field.RecordField;
import slimeknights.mantle.data.loadable.mapping.EitherRecordLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.DataComponentHelper;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Class representing an item stack output. Supports both direct stacks and tag output, behaving like {@link net.minecraft.world.item.crafting.Ingredient} used for output.
 * @see TagPreference
 * @see FluidOutput
 */
@SuppressWarnings("unused")  // API
public abstract sealed class ItemOutput extends StackOutput<Item,ItemStack> {
  /** Empty instance */
  public static final ItemOutput EMPTY = new OfStack(ItemStack.EMPTY);

  /* Constructors */

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
    return fromStack(new ItemStack(item, count));
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
   * @param tag         Tag
   * @param count       Stack count
   * @param components  Stack components
   * @return Output
   */
  public static ItemOutput fromTag(TagKey<Item> tag, int count, DataComponentPatch components) {
    if (count <= 0) {
      return EMPTY;
    }
    return new OfTagPreference(tag, count, components);
  }

  /**
   * Creates a new output for the given tag
   * @param tag   Tag
   * @param count Stack count
   * @return Output
   */
  public static ItemOutput fromTag(TagKey<Item> tag, int count) {
    return fromTag(tag, count, DataComponentPatch.EMPTY);
  }

  /**
   * Creates a new output for the given tag
   * @param tag  Tag
   * @return Output
   */
  public static ItemOutput fromTag(TagKey<Item> tag) {
    return fromTag(tag, 1);
  }


  /* Main methods */

  @Override
  public final ItemStack copy() {
    return get().copy();
  }


  /* Field getters */

  /** {@return the size of the output} Does not resolve the stack. */
  public abstract int getCount();

  @Override
  public boolean isEmpty() {
    return getCount() <= 0;
  }


  /** Class for an output that is just a stack */
  @RequiredArgsConstructor
  private static final class OfStack extends ItemOutput {
    private final ItemStack stack;

    @Override
    public Either<Item, TagKey<Item>> getValueOrTag() {
      return Either.left(stack.getItem());
    }

    @Override
    public DataComponentPatch getComponents() {
      return stack.getComponentsPatch();
    }

    @Override
    public ItemStack get() {
      return stack;
    }

    @Override
    public int getCount() {
      return stack.getCount();
    }
  }

  /** Class for an output from a tag preference */
  @RequiredArgsConstructor
  private static final class OfTagPreference extends ItemOutput {
    private final TagKey<Item> tag;
    @Getter
    private final int count;
    @Getter
    private final DataComponentPatch components;
    private ItemStack cachedResult = null;

    @Override
    public Either<Item, TagKey<Item>> getValueOrTag() {
      return Either.right(tag);
    }

    @Override
    public ItemStack get() {
      // cache the result from the tag preference to save effort, especially helpful if the tag becomes invalid
      // this object should only exist in recipes so no need to invalidate the cache
      if (cachedResult == null) {
        // if the preference is empty, do not cache it.
        // This should only happen if someone scans recipes before tag are computed in which case we cache the wrong result.
        // We protect against empty tags in our recipes via conditions.
        Optional<Item> preference = TagPreference.getPreference(tag);
        if (preference.isEmpty()) {
          return ItemStack.EMPTY;
        }
        cachedResult = new ItemStack(preference.orElseThrow(), count);
        if (!components.isEmpty()) {
          cachedResult.applyComponents(components);
        }
      }
      return cachedResult;
    }
  }


  /* Serializing */

  /** Creates from the codec */
  private static ItemOutput fromCodec(Either<Item,TagKey<Item>> itemOrTag, DataComponentPatch components, int count) {
    if (count <= 0) {
      return EMPTY;
    }
    return itemOrTag.map(
      item -> fromStack(DataComponentHelper.makeStack(item, count, components)),
      tag -> fromTag(tag, count, components)
    );
  }

  /** Creates from the codec */
  private static ItemOutput fromCodec(Either<Item,TagKey<Item>> itemOrTag, DataComponentPatch components) {
    return fromCodec(itemOrTag, components, 1);
  }


  /* Codecs */

  /** Prepared item tag field */
  private static final MapCodec<TagKey<Item>> ITEM_TAG_CODEC = MantleCodecs.ITEM_TAG.fieldOf("tag");
  /** Item tag or required item */
  private static final MapCodec<Either<Item,TagKey<Item>>> REQUIRED_ITEM_OR_TAG = NeoForgeExtraCodecs.xor(MantleCodecs.NON_AIR_ITEM.fieldOf("item"), ITEM_TAG_CODEC);
  /** Item tag or optional item */
  private static final MapCodec<Either<Item,TagKey<Item>>> OPTIONAL_ITEM_OR_TAG = NeoForgeExtraCodecs.xor(BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("item", Items.AIR), ITEM_TAG_CODEC);
  /** Codec serializing a compact output. */
  private static final Codec<ItemOutput> COMPACT_CODEC = MantleCodecs.NON_AIR_ITEM.flatComapMap(ItemOutput::fromItem,
    output -> output.getCount() == 1 && output.getComponents().isEmpty() && output instanceof OfStack stack
      ? DataResult.success(stack.stack.getItem())
      : DataResult.error(() -> "Cannot serialize ItemOutput with count, components, or tags compactly."));

  /** Codec matching a non-empty stack with count. */
  public static final Codec<ItemOutput> REQUIRED_STACK_CODEC = NeoForgeExtraCodecs.withAlternative(COMPACT_CODEC, RecordCodecBuilder.create(instance -> codecStart(instance, REQUIRED_ITEM_OR_TAG).and(
    ExtraCodecs.POSITIVE_INT.fieldOf("count").orElse(1).forGetter(ItemOutput::getCount)
  ).apply(instance, ItemOutput::fromCodec)));
  /** Codec matching a stack that may be empty with count. */
  public static final Codec<ItemOutput> OPTIONAL_STACK_CODEC = NeoForgeExtraCodecs.withAlternative(COMPACT_CODEC, RecordCodecBuilder.create(instance -> codecStart(instance, OPTIONAL_ITEM_OR_TAG).and(
    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("count").orElse(1).forGetter(ItemOutput::getCount)
  ).apply(instance, ItemOutput::fromCodec)));
  /** Codec matching a non-empty stack of size 1. */
  public static final Codec<ItemOutput> REQUIRED_ITEM_CODEC = NeoForgeExtraCodecs.withAlternative(COMPACT_CODEC, RecordCodecBuilder.create(instance -> codecStart(instance, REQUIRED_ITEM_OR_TAG).apply(instance, ItemOutput::fromCodec)));
  /** Codec matching a stack of size 1. */
  public static final Codec<ItemOutput> OPTIONAL_ITEM_CODEC = NeoForgeExtraCodecs.withAlternative(COMPACT_CODEC, RecordCodecBuilder.create(instance -> codecStart(instance, OPTIONAL_ITEM_OR_TAG).apply(instance, ItemOutput::fromCodec)));

  /** Stream codec instance syncing a required stack directly */
  public static final StreamCodec<RegistryFriendlyByteBuf,ItemOutput> REQUIRED_STREAM_CODEC = ItemStack.STREAM_CODEC.map(ItemOutput::fromStack, ItemOutput::get);
  /** Stream codec instance syncing an optional stack directly */
  public static final StreamCodec<RegistryFriendlyByteBuf,ItemOutput> OPTIONAL_STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC.map(ItemOutput::fromStack, ItemOutput::get);


  /* Loadables */
  /** Item tag or required item */
  private static final RecordField<Either<Item,TagKey<Item>>,ItemOutput> REQUIRED_ITEM_OR_TAG_FIELD = new EitherRecordLoadable<>("item", Loadables.NON_EMPTY_ITEM, "tag", Loadables.ITEM_TAG).directField(ItemOutput::getValueOrTag);
  /** Item tag or optional item */
  private static final RecordField<Either<Item,TagKey<Item>>,ItemOutput> OPTIONAL_ITEM_OR_TAG_FIELD = new EitherRecordLoadable<>("item", Loadables.ITEM, "tag", Loadables.ITEM_TAG, Either.left(Items.AIR)).directField(ItemOutput::getValueOrTag);
  /** Network logic for required stacks */
  private static final Streamable<ItemOutput> REQUIRED_STREAMABLE = new CodecStreamable<>(REQUIRED_STREAM_CODEC);
  /** Network logic for optional stacks */
  private static final Streamable<ItemOutput> OPTIONAL_STREAMABLE = new CodecStreamable<>(OPTIONAL_STREAM_CODEC);

  /** Compact loadable for item string */
  private static final StringLoadable<ItemOutput> COMPACT_LOADABLE = Loadables.ITEM.flatXmap(ItemOutput::fromItem, output -> output.get().getItem());
  /** Predicate for when to serialize compactly */
  private static final Predicate<ItemOutput> IS_COMPACT = output -> output.getCount() == 1 && output.getComponents().isEmpty() && output instanceof OfStack;

  /** Applies the compact and network properties to the loadable */
  private static RecordLoadable<ItemOutput> finishLoadable(RecordLoadable<ItemOutput> loadable, Streamable<ItemOutput> network) {
    return loadable.compact(COMPACT_LOADABLE, IS_COMPACT).withNetwork(network);
  }

  /** Loadable matching a non-empty stack with count. */
  public static final RecordLoadable<ItemOutput> REQUIRED_STACK_LOADABLE = finishLoadable(RecordLoadable.create(REQUIRED_ITEM_OR_TAG_FIELD, COMPONENTS_FIELD, IntLoadable.FROM_ONE.defaultField("count", 1, true, ItemOutput::getCount), ItemOutput::fromCodec), REQUIRED_STREAMABLE);
  /** Loadable matching a stack that may be empty with count. */
  public static final RecordLoadable<ItemOutput> OPTIONAL_STACK_LOADABLE = finishLoadable(RecordLoadable.create(OPTIONAL_ITEM_OR_TAG_FIELD, COMPONENTS_FIELD, IntLoadable.FROM_ZERO.defaultField("count", 1, true, ItemOutput::getCount), ItemOutput::fromCodec), OPTIONAL_STREAMABLE);
  /** Loadable matching a non-empty stack of size 1. */
  public static final RecordLoadable<ItemOutput> REQUIRED_ITEM_LOADABLE = finishLoadable(RecordLoadable.create(REQUIRED_ITEM_OR_TAG_FIELD, COMPONENTS_FIELD, ItemOutput::fromCodec), REQUIRED_STREAMABLE);
  /** Loadable matching a stack of size 1. */
  public static final RecordLoadable<ItemOutput> OPTIONAL_ITEM_LOADABLE = finishLoadable(RecordLoadable.create(OPTIONAL_ITEM_OR_TAG_FIELD, COMPONENTS_FIELD, ItemOutput::fromCodec), OPTIONAL_STREAMABLE);

  @Deprecated(forRemoval = true)
  public static class Loadable {
    /** @deprecated use {@link #OPTIONAL_ITEM_LOADABLE} */
    @Deprecated(forRemoval = true)
    public static final RecordLoadable<ItemOutput> OPTIONAL_ITEM = OPTIONAL_ITEM_LOADABLE;
    /** @deprecated use {@link #OPTIONAL_STACK_LOADABLE} */
    @Deprecated(forRemoval = true)
    public static final RecordLoadable<ItemOutput> OPTIONAL_STACK = OPTIONAL_STACK_LOADABLE;
    /** @deprecated use {@link #REQUIRED_ITEM_LOADABLE} */
    @Deprecated(forRemoval = true)
    public static final RecordLoadable<ItemOutput> REQUIRED_ITEM = REQUIRED_ITEM_LOADABLE;
    /** @deprecated use {@link #REQUIRED_STACK_LOADABLE} */
    @Deprecated(forRemoval = true)
    public static final RecordLoadable<ItemOutput> REQUIRED_STACK = REQUIRED_STACK_LOADABLE;
  }
}
