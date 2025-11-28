package slimeknights.mantle.recipe.ingredient;

import com.google.gson.JsonElement;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.LoadableIngredientSerializer;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/** Simple ingredient checking for an item with a specific potion */
public class PotionIngredient extends ItemIngredient {
  /** Ingredient serializer instance */
  public static final LoadableIngredientSerializer<PotionIngredient> SERIALIZER = new LoadableIngredientSerializer<>(RecordLoadable.create(
    ItemsField.INSTANCE, TAG_FIELD,
    Loadables.POTION.defaultField("potion", Potions.EMPTY, false, i -> i.potion),
    PotionIngredient::new
  ));

  private final Potion potion;
  protected PotionIngredient(List<Item> items, @Nullable TagKey<Item> itemTag, Potion potion) {
    // potion is added in directly to the parent value stream
    super(items, itemTag, Stream.concat(
      items.stream().map(item -> new ItemValue(PotionUtils.setPotion(new ItemStack(item), potion))),
      Stream.ofNullable(itemTag).map(tag -> new PotionTagValue(tag, potion)))
    );
    this.potion = potion;
  }

  /** Creates a potion ingredient matching a list of items */
  public static PotionIngredient of(Potion potion, List<ItemLike> items) {
    return new PotionIngredient(toItem(items), null, potion);
  }

  /** Creates a potion ingredient matching a list of items */
  public static PotionIngredient of(Potion potion, ItemLike... items) {
    return of(potion, Arrays.asList(items));
  }

  /** Creates a potion ingredient matching a tag */
  public static PotionIngredient of(Potion potion, TagKey<Item> tag) {
    return new PotionIngredient(List.of(), tag, potion);
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    // stack must match, any item must match, and potion must match
    return stack != null && super.test(stack) && PotionUtils.getPotion(stack) == potion;
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IIngredientSerializer<? extends Ingredient> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public JsonElement toJson() {
    return SERIALIZER.serialize(this);
  }

  /** Tag value that sets the potion on each returned item */
  private static class PotionTagValue extends TagValue {
    private final Potion potion;
    public PotionTagValue(TagKey<Item> tag, Potion potion) {
      super(tag);
      this.potion = potion;
    }

    @Override
    public Collection<ItemStack> getItems() {
      return super.getItems().stream()
        .map(item -> PotionUtils.setPotion(item, potion))
        .toList();
    }
  }
}
