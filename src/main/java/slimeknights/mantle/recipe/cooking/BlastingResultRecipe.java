package slimeknights.mantle.recipe.cooking;

import lombok.Getter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;

/** Extension of {@link BlastingRecipe} to support {@link ItemOutput} */
@Getter
public class BlastingResultRecipe extends BlastingRecipe implements CookingResultRecipe {
  public static LoadableField<Integer, AbstractCookingRecipe> COOKING_TIME_FIELD = IntLoadable.FROM_ONE.defaultField("cooking_time", 100, true, AbstractCookingRecipe::getCookingTime);
  public static final RecordLoadable<BlastingResultRecipe> LOADABLE = RecordLoadable.create(
    ContextKey.ID.requiredField(), LoadableRecipeSerializer.RECIPE_GROUP, CookingResultRecipe.CATEGORY_FIELD,
    IngredientLoadable.DISALLOW_EMPTY.requiredField("ingredient", r -> r.ingredient),
    RESULT_FIELD, EXPERIENCE_FIELD, COOKING_TIME_FIELD,
    BlastingResultRecipe::new);

  private final ItemOutput result;
  public BlastingResultRecipe(ResourceLocation id, String group, CookingBookCategory category, Ingredient ingredient, ItemOutput result, float experience, int cookingTime) {
    super(id, group, category, ingredient, ItemStack.EMPTY, experience, cookingTime);
    this.result = result;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return MantleRecipes.BLASTING.get();
  }

  @Override
  public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
    return result.get();
  }

  @Override
  public ItemStack assemble(Container pContainer, RegistryAccess pRegistryAccess) {
    return result.copy();
  }
}
