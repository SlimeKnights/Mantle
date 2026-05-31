package slimeknights.mantle.compat.neoforged.neoforge.common.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractIngredient {
  private final Value[] values;
  @Nullable
  private ItemStack[] itemStacks;

  protected AbstractIngredient(Stream<? extends Value> values) {
    this.values = values.toArray(Value[]::new);
  }

  public boolean test(@Nullable ItemStack stack) {
    if (stack == null || stack.isEmpty()) {
      return false;
    }
    for (ItemStack item : getItems()) {
      if (item.is(stack.getItem())) {
        return true;
      }
    }
    return false;
  }

  public ItemStack[] getItems() {
    if (itemStacks == null) {
      itemStacks = Arrays.stream(values).flatMap(value -> value.getItems().stream()).toArray(ItemStack[]::new);
    }
    return itemStacks;
  }

  protected void invalidate() {
    itemStacks = null;
  }

  public boolean isSimple() {
    return true;
  }

  public boolean isEmpty() {
    return values.length == 0;
  }

  public JsonElement toJson() {
    throw new UnsupportedOperationException();
  }

  public abstract IIngredientSerializer<?> getSerializer();

  public interface Value {
    Collection<ItemStack> getItems();

    JsonObject serialize();
  }

  public static class ItemValue implements Value {
    private final ItemStack item;

    public ItemValue(ItemStack item) {
      this.item = item;
    }

    @Override
    public Collection<ItemStack> getItems() {
      return List.of(item);
    }

    @Override
    public JsonObject serialize() {
      JsonObject json = new JsonObject();
      json.addProperty("item", net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item.getItem()).toString());
      return json;
    }
  }

  public static class TagValue implements Value {
    protected final TagKey<Item> tag;

    public TagValue(TagKey<Item> tag) {
      this.tag = tag;
    }

    @Override
    public Collection<ItemStack> getItems() {
      return net.minecraft.core.registries.BuiltInRegistries.ITEM.getTagOrEmpty(tag).iterator().hasNext()
        ? java.util.stream.StreamSupport.stream(net.minecraft.core.registries.BuiltInRegistries.ITEM.getTagOrEmpty(tag).spliterator(), false).map(holder -> new ItemStack(holder.value())).toList()
        : List.of();
    }

    @Override
    public JsonObject serialize() {
      JsonObject json = new JsonObject();
      json.addProperty("tag", tag.location().toString());
      return json;
    }
  }
}
