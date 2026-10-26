package slimeknights.mantle.plugin.jei;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeType;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.fluid.transfer.IDisplayFluidTransfer;
import slimeknights.mantle.recipe.NamedRecipe;
import slimeknights.mantle.recipe.ingredient.EntityIngredient.EntityInput;

/** Any JEI types or simple helpers registered by Mantle. */
public class MantleJEIConstants {
  /** Ingredient for an entity */
  public static final IIngredientType<EntityInput> ENTITY_TYPE = () -> EntityInput.class;
  /** Recipe type for the fluid transfer category. Only enabled if {@link #enableTransferTab()} is called. Use {@link #isTransferTabEnabled()} to optionally depend on it. */
  public static final RecipeType<NamedRecipe<IDisplayFluidTransfer>> FLUID_TRANSFER = RecipeType.create(Mantle.modId, "fluid_transfer", NamedRecipe.<IDisplayFluidTransfer>recipeClass());


  /* Transfer enabling */

  private static boolean enableTransferTab = false;

  /** Call during constructor for a {@link mezz.jei.api.IModPlugin} to enable the {@link #FLUID_TRANSFER} tab. Should be done alongside adding catalysts to the tab. */
  public static void enableTransferTab() {
    enableTransferTab = true;
  }

  /** If true, {@link #FLUID_TRANSFER} will have its tab registered. Call {@link #enableTransferTab()} to enable. */
  public static boolean isTransferTabEnabled() {
    return enableTransferTab;
  }
}
