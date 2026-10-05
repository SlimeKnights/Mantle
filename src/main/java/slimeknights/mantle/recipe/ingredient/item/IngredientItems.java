package slimeknights.mantle.recipe.ingredient.item;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.data.MantleCodecs;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.array.ArrayLoadable;
import slimeknights.mantle.data.loadable.field.RecordField;
import slimeknights.mantle.data.loadable.field.UnsyncedField;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.network.MantleStreamCodecs;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Data object for ingredients wishing to match standard vanilla-style items and tags.
 * Not meant to be used directly in recipes. Just use a traditional {@link net.minecraft.world.item.crafting.Ingredient} for more flexibility.
 */
@RequiredArgsConstructor
public final class IngredientItems implements Predicate<ItemStack> {
  /** Loadable instance for nesting inside other loadables. */
  public static final RecordLoadable<IngredientItems> LOADABLE = RecordLoadable.create(
    ItemsField.INSTANCE,
    new UnsyncedField<>(Loadables.ITEM_TAG.nullableField("tag", i -> i.tag)),
    IngredientItems::new);
  /** Map codec for nesting this inside another codec. */
  public static final MapCodec<IngredientItems> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    MantleCodecs.compactList(BuiltInRegistries.ITEM.byNameCodec()).fieldOf("item").forGetter(i -> i.items),
    MantleCodecs.ITEM_TAG.optionalFieldOf("tag").forGetter(i -> Optional.ofNullable(i.tag))
  ).apply(instance, (items, tag) -> new IngredientItems(items, tag.orElse(null))));
  /** Stream codec for networking, syncing just items. */
  public static final StreamCodec<RegistryFriendlyByteBuf,IngredientItems> STREAM_CODEC = StreamCodec.composite(
    MantleStreamCodecs.ITEM.apply(ByteBufCodecs.list()), IngredientItems::getAllItems,
    items -> new IngredientItems(items, null));

  /** Items from JSON */
  private final List<Item> items;
  /** Tag key from JSON */
  @Nullable
  private final TagKey<Item> tag;
  /** Combined items from tag and list */
  private List<Item> allItems;

  /** Creates from a list of items */
  public static IngredientItems of(List<ItemLike> items) {
    return new IngredientItems(items.stream().map(ItemLike::asItem).toList(), null);
  }

  /** Creates from a list of items */
  public static IngredientItems of(ItemLike... items) {
    return of(List.of(items));
  }

  /** Creates from a tag key */
  public static IngredientItems of(TagKey<Item> tag) {
    return new IngredientItems(List.of(), tag);
  }

  @Override
  public boolean test(ItemStack stack) {
    return items.contains(stack.getItem()) || tag != null && stack.is(tag);
  }

  /** Gets all items in this ingredient. Used to construct display stacks and to sync over the network. */
  public List<Item> getAllItems() {
    if (allItems == null) {
      if (tag == null) {
        allItems = items;
      } else {
        allItems = Stream.concat(
          items.stream(),
          RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, tag)
        ).toList();
      }
    }
    return allItems;
  }

  /** Custom field that syncs the item tag as items to the client */
  private enum ItemsField implements RecordField<List<Item>, IngredientItems> {
    INSTANCE;

    private static final Loadable<List<Item>> ITEM_LIST = Loadables.ITEM.list(ArrayLoadable.COMPACT_OR_EMPTY);

    @Override
    public List<Item> get(JsonObject json, TypedMap context) {
      return ITEM_LIST.getOrDefault(json, "item", List.of(), context);
    }

    @Override
    public void serializeInto(IngredientItems parent, JsonObject json, TypedMap context) {
      if (!parent.items.isEmpty()) {
        json.add("item", ITEM_LIST.serialize(parent.items));
      }
    }

    @Override
    public List<Item> decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
      return ITEM_LIST.decode(buffer, context);
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer, IngredientItems parent, TypedMap context) {
      // sync both tag and item values to client
      ITEM_LIST.encode(buffer, parent.getAllItems(), context);
    }
  }
}
