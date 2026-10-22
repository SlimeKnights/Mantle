package slimeknights.mantle.recipe;

import net.neoforged.bus.api.IEventBus;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.cooking.BlastingResultRecipe;
import slimeknights.mantle.recipe.cooking.CampfireResultRecipe;
import slimeknights.mantle.recipe.cooking.SmeltingResultRecipe;
import slimeknights.mantle.recipe.cooking.SmokingResultRecipe;
import slimeknights.mantle.recipe.crafting.fallback.ShapedFallbackRecipe;
import slimeknights.mantle.recipe.crafting.fallback.ShapelessFallbackRecipe;
import slimeknights.mantle.recipe.crafting.retextured.ShapedRetexturedRecipe;
import slimeknights.mantle.recipe.ingredient.fluid.PotionFluidIngredient;
import slimeknights.mantle.recipe.ingredient.item.FluidContainerIngredient;
import slimeknights.mantle.recipe.ingredient.item.PotionDisplayIngredient;
import slimeknights.mantle.recipe.ingredient.item.PotionIngredient;
import slimeknights.mantle.registration.deferred.FluidIngredientDeferredRegister;
import slimeknights.mantle.registration.deferred.IngredientDeferredRegister;
import slimeknights.mantle.registration.deferred.RecipeSerializerDeferredRegister;
import slimeknights.mantle.registration.holder.DeferredFluidIngredient;
import slimeknights.mantle.registration.holder.DeferredIngredient;
import slimeknights.mantle.registration.holder.DeferredRecipeSerializer;

/** Handles any custom recipes and conditions added by Mantle */
public class MantleRecipes {
  private static final RecipeSerializerDeferredRegister RECIPES = new RecipeSerializerDeferredRegister(Mantle.modId);
  private static final IngredientDeferredRegister INGREDIENT_TYPES = new IngredientDeferredRegister(Mantle.modId);
  private static final FluidIngredientDeferredRegister FLUID_INGREDIENT_TYPES = new FluidIngredientDeferredRegister(Mantle.modId);

  private MantleRecipes() {}

  /** Registers this to the bus */
  public static void init(IEventBus bus) {
    RECIPES.register(bus);
    INGREDIENT_TYPES.register(bus);
    FLUID_INGREDIENT_TYPES.register(bus);
  }

  // crafting
  public static final DeferredRecipeSerializer<ShapedFallbackRecipe> CRAFTING_SHAPED_FALLBACK = RECIPES.registerSimple("crafting_shaped_fallback", ShapedFallbackRecipe.CODEC, ShapedFallbackRecipe.STREAM_CODEC);
  public static final DeferredRecipeSerializer<ShapelessFallbackRecipe> CRAFTING_SHAPELESS_FALLBACK = RECIPES.registerSimple("crafting_shapeless_fallback", ShapelessFallbackRecipe.CODEC, ShapelessFallbackRecipe.STREAM_CODEC);
  public static final DeferredRecipeSerializer<ShapedRetexturedRecipe> CRAFTING_SHAPED_RETEXTURED = RECIPES.registerSimple("crafting_shaped_retextured", ShapedRetexturedRecipe.CODEC, ShapedRetexturedRecipe.STREAM_CODEC);
  // cooking
  public static final DeferredRecipeSerializer<SmeltingResultRecipe> SMELTING = RECIPES.registerLoadable("smelting", SmeltingResultRecipe.LOADABLE);
  public static final DeferredRecipeSerializer<BlastingResultRecipe> BLASTING = RECIPES.registerLoadable("blasting", BlastingResultRecipe.LOADABLE);
  public static final DeferredRecipeSerializer<SmokingResultRecipe> SMOKING = RECIPES.registerLoadable("smoking", SmokingResultRecipe.LOADABLE);
  public static final DeferredRecipeSerializer<CampfireResultRecipe> CAMPFIRE = RECIPES.registerLoadable("campfire", CampfireResultRecipe.LOADABLE);

  // ingredients
  public static final DeferredIngredient<PotionIngredient> POTION_INGREDIENT = INGREDIENT_TYPES.register("potion", PotionIngredient.CODEC, PotionIngredient.STREAM_CODEC);
  public static final DeferredIngredient<PotionDisplayIngredient> POTION_DISPLAY_INGREDIENT = INGREDIENT_TYPES.register("potion_display", PotionDisplayIngredient.CODEC, PotionDisplayIngredient.STREAM_CODEC);
  public static final DeferredIngredient<FluidContainerIngredient> FLUID_CONTAINER_INGREDIENT = INGREDIENT_TYPES.register("fluid_container", FluidContainerIngredient.CODEC, FluidContainerIngredient.STREAM_CODEC);

  // fluid ingredients
  public static final DeferredFluidIngredient<PotionFluidIngredient> POTION_FLUID_INGREDIENT = FLUID_INGREDIENT_TYPES.register("potion", PotionFluidIngredient.CODEC, PotionFluidIngredient.STREAM_CODEC);
}
