package slimeknights.mantle.recipe.ingredient;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.array.ArrayLoadable;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.field.RecordField;
import slimeknights.mantle.data.loadable.field.UnsyncedField;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;
import java.util.stream.Stream;

/** Abstract ingredient that matches a list of items or a tag, mirroring the vanilla syntax */
public abstract class ItemIngredient implements ICustomIngredient {
  /** Field for the item tag */
  protected static final LoadableField<TagKey<Item>,ItemIngredient> TAG_FIELD = new UnsyncedField<>(Loadables.ITEM_TAG.nullableField("tag", i -> i.tag));

  protected final List<Item> items;
  @Nullable
  protected final TagKey<Item> tag;
  @Nullable
  private ItemStack[] itemStacks;

  /** Constructor letting you supply your own item stream */
  protected ItemIngredient(List<Item> items, @Nullable TagKey<Item> tag) {
    this.items = items;
    this.tag = tag;
  }

  /** Maps the list to a list of items */
  protected static List<Item> toItem(List<ItemLike> items) {
    return items.stream().map(ItemLike::asItem).toList();
  }

  @Override
  public boolean test(ItemStack stack) {
    // super is going to do list iteration, but for tag checks it's way easier to just check directly
    // also ensures we never match empty just because our lists are empty
    return !stack.isEmpty() && (items.contains(stack.getItem()) || tag != null && stack.is(tag));
  }

  @Override
  public Stream<ItemStack> getItems() {
    if (itemStacks == null) {
      itemStacks = Stream.concat(
        items.stream().map(ItemStack::new),
        Stream.ofNullable(tag).flatMap(ItemIngredient::getTagItems)
      ).toArray(ItemStack[]::new);
    }
    return Arrays.stream(itemStacks);
  }

  private static Stream<ItemStack> getTagItems(TagKey<Item> tag) {
    return StreamSupport.stream(BuiltInRegistries.ITEM.getTagOrEmpty(tag).spliterator(), false).map(holder -> new ItemStack(holder.value()));
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public boolean equals(Object object) {
    return this == object || object != null && object.getClass() == getClass() && object instanceof ItemIngredient that && items.equals(that.items) && Objects.equals(tag, that.tag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClass(), items, tag);
  }

  /** Custom field that syncs the item tag as items to the client */
  public enum ItemsField implements RecordField<List<Item>,ItemIngredient> {
    INSTANCE;

    private static final Loadable<List<Item>> ITEM_LIST = Loadables.ITEM.list(ArrayLoadable.COMPACT_OR_EMPTY);

    @Override
    public List<Item> get(JsonObject json, TypedMap context) {
      return ITEM_LIST.getOrDefault(json, "item", List.of(), context);
    }

    @Override
    public void serialize(ItemIngredient parent, JsonObject json) {
      if (!parent.items.isEmpty()) {
        json.add("item", ITEM_LIST.serialize(parent.items));
      }
    }

    @Override
    public List<Item> decode(FriendlyByteBuf buffer, TypedMap context) {
      return ITEM_LIST.decode(buffer, context);
    }

    @Override
    public void encode(FriendlyByteBuf buffer, ItemIngredient parent) {
      // sync both tag and item values to client
      ITEM_LIST.encode(buffer, parent.getItems().map(ItemStack::getItem).toList());
    }
  }
}
