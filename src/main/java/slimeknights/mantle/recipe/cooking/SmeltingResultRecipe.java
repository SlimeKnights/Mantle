package slimeknights.mantle.recipe.cooking;

import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;

/** Extension of {@link SmeltingRecipe} to support {@link ItemOutput} */
@Getter
public class SmeltingResultRecipe extends SmeltingRecipe implements CookingResultRecipe {
  public static LoadableField<Integer, AbstractCookingRecipe> COOKING_TIME_FIELD = IntLoadable.FROM_ONE.defaultField("cooking_time", 200, true, AbstractCookingRecipe::getCookingTime);
  public static final RecordLoadable<SmeltingResultRecipe> LOADABLE = RecordLoadable.create(
    LoadableRecipeSerializer.RECIPE_GROUP, CookingResultRecipe.CATEGORY_FIELD,
    IngredientLoadable.DISALLOW_EMPTY.requiredField("ingredient", r -> r.ingredient),
    RESULT_FIELD, EXPERIENCE_FIELD, COOKING_TIME_FIELD,
    SmeltingResultRecipe::new);

  private final ItemOutput result;
  public SmeltingResultRecipe(String group, CookingBookCategory category, Ingredient ingredient, ItemOutput result, float experience, int cookingTime) {
    super(group, category, ingredient, ItemStack.EMPTY, experience, cookingTime);
    this.result = result;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return MantleRecipes.SMELTING.get();
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider pRegistryAccess) {
    return result.get();
  }

  @Override
  public ItemStack assemble(SingleRecipeInput pContainer, HolderLookup.Provider pRegistryAccess) {
    return result.copy();
  }
}
