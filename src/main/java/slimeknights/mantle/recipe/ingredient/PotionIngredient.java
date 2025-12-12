package slimeknights.mantle.recipe.ingredient;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.LoadableIngredientSerializer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Simple ingredient checking for an item with a specific potion.
 * 
 * In NeoForge 1.21+, PotionUtils was removed. Potions are now stored via DataComponents.POTION_CONTENTS.
 * 
 * @deprecated The ingredient system has changed in 1.21+. Consider using ICustomIngredient
 *             with DataComponentIngredient for potion matching.
 */
@Deprecated(forRemoval = true)
public class PotionIngredient extends ItemIngredient {
  /** Ingredient serializer instance - deprecated, no longer functional */
  @Deprecated(forRemoval = true)
  public static final LoadableIngredientSerializer<PotionIngredient> SERIALIZER = new LoadableIngredientSerializer<>(RecordLoadable.create(
    ItemsField.INSTANCE, TAG_FIELD,
    Loadables.POTION.defaultField("potion", Potions.WATER, false, i -> i.potion),
    PotionIngredient::new
  ));

  private final Potion potion;
  
  protected PotionIngredient(List<Item> items, @Nullable TagKey<Item> itemTag, Potion potion) {
    super(items, itemTag);
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

  /** Gets the potion from a stack using the new data component system */
  private static Optional<Potion> getPotionFromStack(ItemStack stack) {
    PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
    if (contents != null && contents.potion().isPresent()) {
      return contents.potion().map(holder -> holder.value());
    }
    return Optional.empty();
  }

  /** Sets the potion on a stack using the new data component system */
  private static ItemStack setPotion(ItemStack stack, Potion potion) {
    stack.set(DataComponents.POTION_CONTENTS, new PotionContents(BuiltInRegistries.POTION.wrapAsHolder(potion)));
    return stack;
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    if (stack == null || !super.test(stack)) {
      return false;
    }
    return getPotionFromStack(stack).map(p -> p == potion).orElse(false);
  }

  @Override
  public ItemStack[] getItems() {
    ItemStack[] parentItems = super.getItems();
    return Arrays.stream(parentItems)
        .map(item -> setPotion(item.copy(), potion))
        .toArray(ItemStack[]::new);
  }
}
