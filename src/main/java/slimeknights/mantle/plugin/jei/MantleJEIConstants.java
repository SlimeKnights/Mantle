package slimeknights.mantle.plugin.jei;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeType;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.fluid.transfer.IDisplayFluidTransfer;
import slimeknights.mantle.recipe.NamedRecipe;
import slimeknights.mantle.recipe.ingredient.EntityIngredient.EntityInput;

public class MantleJEIConstants {
  /** Ingredient for an entity */
  public static final IIngredientType<EntityInput> ENTITY_TYPE = () -> EntityInput.class;
  /** Recipe type for the fluid transfer category */
  public static final RecipeType<NamedRecipe<IDisplayFluidTransfer>> FLUID_TRANSFER = RecipeType.create(Mantle.modId, "fluid_transfer", NamedRecipe.<IDisplayFluidTransfer>recipeClass());
}
