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
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.LoadableIngredientSerializer;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * Ingredient that shows all potion variants on the displayed item list.
 * 
 * In NeoForge 1.21+, PotionUtils was removed. Potions are now stored via DataComponents.POTION_CONTENTS.
 * 
 * @deprecated The ingredient system has changed in 1.21+. Consider using ICustomIngredient
 *             with DataComponentIngredient for potion matching.
 */
@Deprecated(forRemoval = true)
public class PotionDisplayIngredient extends ItemIngredient {
  /** Ingredient serializer instance - deprecated, no longer functional */
  @Deprecated(forRemoval = true)
  public static final LoadableIngredientSerializer<PotionDisplayIngredient> SERIALIZER = new LoadableIngredientSerializer<>(RecordLoadable.create(ItemsField.INSTANCE, TAG_FIELD, PotionDisplayIngredient::new));

  /** last return of {@link #getItems()} */
  private ItemStack[] lastParentStacks = null;
  /** cache for {@link #getItems()} */
  private ItemStack[] displayStacks = null;

  protected PotionDisplayIngredient(List<Item> items, @Nullable TagKey<Item> tag) {
    super(items, tag);
  }

  /** Creates a ingredient matching a list of items */
  public static PotionDisplayIngredient of(List<ItemLike> items) {
    return new PotionDisplayIngredient(toItem(items), null);
  }

  /** Creates a ingredient matching a list of items */
  public static PotionDisplayIngredient of(ItemLike... items) {
    return of(List.of(items));
  }

  /** Creates a ingredient matching a tag */
  public static PotionDisplayIngredient of(TagKey<Item> tag) {
    return new PotionDisplayIngredient(List.of(), tag);
  }

  /** Sets the potion on a stack using the new data component system */
  private static ItemStack setPotion(ItemStack stack, Potion potion) {
    ItemStack copy = stack.copy();
    copy.set(DataComponents.POTION_CONTENTS, new PotionContents(BuiltInRegistries.POTION.wrapAsHolder(potion)));
    return copy;
  }

  @Override
  public ItemStack[] getItems() {
    // if empty, means we want wildcard, show all potions on the stack
    ItemStack[] parentStacks = super.getItems();
    if (lastParentStacks != parentStacks) {
      lastParentStacks = parentStacks;
      displayStacks = BuiltInRegistries.POTION.stream()
        .filter(pot -> pot != Potions.WATER) // EMPTY became WATER in 1.21
        .flatMap(pot -> Arrays.stream(parentStacks).map(item -> setPotion(item, pot)))
        .toArray(ItemStack[]::new);
    }
    return displayStacks;
  }
}
