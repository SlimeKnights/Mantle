package slimeknights.mantle.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.cooking.BlastingResultRecipe;
import slimeknights.mantle.recipe.cooking.CampfireResultRecipe;
import slimeknights.mantle.recipe.cooking.SmeltingResultRecipe;
import slimeknights.mantle.recipe.cooking.SmokingResultRecipe;
import slimeknights.mantle.recipe.crafting.ShapedFallbackRecipe;
import slimeknights.mantle.recipe.crafting.ShapedRetexturedRecipe;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.mantle.recipe.ingredient.FluidContainerIngredient;
import slimeknights.mantle.recipe.ingredient.LegacyIngredient;
import slimeknights.mantle.recipe.ingredient.PotionDisplayIngredient;
import slimeknights.mantle.recipe.ingredient.PotionIngredient;

/** Handles any custom recipes added by Mantle */
public class MantleRecipes {
  private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Mantle.modId);
  private static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Mantle.modId);

  private MantleRecipes() {}

  /** Registers this to the bus */
  public static void init(IEventBus bus) {
    RECIPES.register(bus);
    INGREDIENT_TYPES.register(bus);
  }

  // crafting
  public static final DeferredHolder<RecipeSerializer<?>,ShapedFallbackRecipe.Serializer> CRAFTING_SHAPED_FALLBACK = RECIPES.register("crafting_shaped_fallback", ShapedFallbackRecipe.Serializer::new);
  public static final DeferredHolder<RecipeSerializer<?>,ShapedRetexturedRecipe.Serializer> CRAFTING_SHAPED_RETEXTURED = RECIPES.register("crafting_shaped_retextured", ShapedRetexturedRecipe.Serializer::new);
  // cooking
  public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<SmeltingResultRecipe>> SMELTING = RECIPES.register("smelting", () -> LoadableRecipeSerializer.of(SmeltingResultRecipe.LOADABLE));
  public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<BlastingResultRecipe>> BLASTING = RECIPES.register("blasting", () -> LoadableRecipeSerializer.of(BlastingResultRecipe.LOADABLE));
  public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<SmokingResultRecipe>> SMOKING = RECIPES.register("smoking", () -> LoadableRecipeSerializer.of(SmokingResultRecipe.LOADABLE));
  public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<CampfireResultRecipe>> CAMPFIRE = RECIPES.register("campfire", () -> LoadableRecipeSerializer.of(CampfireResultRecipe.LOADABLE));

  // ingredients
  public static final DeferredHolder<IngredientType<?>,IngredientType<LegacyIngredient<PotionIngredient>>> POTION_INGREDIENT = INGREDIENT_TYPES.register("potion", () -> LegacyIngredient.type(PotionIngredient.SERIALIZER, () -> NeoForgeRegistries.INGREDIENT_TYPES.get(Mantle.getResource("potion"))));
  public static final DeferredHolder<IngredientType<?>,IngredientType<LegacyIngredient<PotionDisplayIngredient>>> POTION_DISPLAY_INGREDIENT = INGREDIENT_TYPES.register("potion_display", () -> LegacyIngredient.type(PotionDisplayIngredient.SERIALIZER, () -> NeoForgeRegistries.INGREDIENT_TYPES.get(Mantle.getResource("potion_display"))));
  public static final DeferredHolder<IngredientType<?>,IngredientType<LegacyIngredient<FluidContainerIngredient>>> FLUID_CONTAINER_INGREDIENT = INGREDIENT_TYPES.register("fluid_container", () -> LegacyIngredient.type(FluidContainerIngredient.SERIALIZER, () -> NeoForgeRegistries.INGREDIENT_TYPES.get(Mantle.getResource("fluid_container"))));
}
