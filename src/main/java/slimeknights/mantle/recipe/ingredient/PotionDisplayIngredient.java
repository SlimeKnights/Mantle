package slimeknights.mantle.recipe.ingredient;

import com.google.gson.JsonElement;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.compat.neoforged.neoforge.common.crafting.IIngredientSerializer;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.recipe.helper.LoadableIngredientSerializer;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/** Ingredient that shows all potion variants on the displayed item list */
public class PotionDisplayIngredient extends ItemIngredient {
  /** Ingredient serializer instance */
  public static final LoadableIngredientSerializer<PotionDisplayIngredient> SERIALIZER = new LoadableIngredientSerializer<>(RecordLoadable.create(ItemsField.INSTANCE, TAG_FIELD, PotionDisplayIngredient::new));

  /** last return of {@link Ingredient#getItems()} */
  private ItemStack[] lastParentStacks = null;
  /** cache for {@link #getItems()} */
  private ItemStack[] displayStacks = null;

  protected PotionDisplayIngredient(List<Item> items, @Nullable TagKey<Item> tag) {
    super(items, tag);
  }

  /** Creates a ingredient matching a list of items */
  public static Ingredient of(List<ItemLike> items) {
    return new LegacyIngredient<>(new PotionDisplayIngredient(toItem(items), null), MantleRecipes.POTION_DISPLAY_INGREDIENT::get).toVanilla();
  }

  /** Creates a ingredient matching a list of items */
  public static Ingredient of(ItemLike... items) {
    return of(List.of(items));
  }

  /** Creates a ingredient matching a tag */
  public static Ingredient of(TagKey<Item> tag) {
    return new LegacyIngredient<>(new PotionDisplayIngredient(List.of(), tag), MantleRecipes.POTION_DISPLAY_INGREDIENT::get).toVanilla();
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public ItemStack[] getItems() {
    // if empty, means we want wildcard, show all potions on the stack
    ItemStack[] parentStacks = super.getItems();
    if (lastParentStacks != parentStacks) {
      lastParentStacks = parentStacks;
      displayStacks = BuiltInRegistries.POTION.stream()
        .filter(pot -> pot != Potions.WATER.value())
        .flatMap(pot -> Arrays.stream(parentStacks).map(item -> setPotion(item.copy(), pot)))
        .toArray(ItemStack[]::new);
    }
    return displayStacks;
  }

  private static ItemStack setPotion(ItemStack stack, Potion potion) {
    stack.set(DataComponents.POTION_CONTENTS, new PotionContents(BuiltInRegistries.POTION.wrapAsHolder(potion)));
    return stack;
  }

  @Override
  public IIngredientSerializer<?> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public JsonElement toJson() {
    return SERIALIZER.serialize(this);
  }
}
