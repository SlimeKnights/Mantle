package slimeknights.mantle.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;
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
import slimeknights.mantle.recipe.helper.SimpleRecipeSerializer;
import slimeknights.mantle.recipe.ingredient.fluid.PotionFluidIngredient;
import slimeknights.mantle.recipe.ingredient.item.FluidContainerIngredient;
import slimeknights.mantle.recipe.ingredient.item.PotionDisplayIngredient;
import slimeknights.mantle.recipe.ingredient.item.PotionIngredient;

/** Handles any custom recipes and conditions added by Mantle */
public class MantleRecipes {
  private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Mantle.modId);
  private static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Mantle.modId);
  private static final DeferredRegister<FluidIngredientType<?>> FLUID_INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_INGREDIENT_TYPES, Mantle.modId);

  private MantleRecipes() {}

  /** Registers this to the bus */
  public static void init(IEventBus bus) {
    RECIPES.register(bus);
    INGREDIENT_TYPES.register(bus);
    FLUID_INGREDIENT_TYPES.register(bus);
  }

  // crafting
  public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<ShapedFallbackRecipe>> CRAFTING_SHAPED_FALLBACK = RECIPES.register("crafting_shaped_fallback", () -> new SimpleRecipeSerializer<>(ShapedFallbackRecipe.CODEC, ShapedFallbackRecipe.STREAM_CODEC));
  public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<ShapedRetexturedRecipe>> CRAFTING_SHAPED_RETEXTURED = RECIPES.register("crafting_shaped_retextured", () -> new SimpleRecipeSerializer<>(ShapedRetexturedRecipe.CODEC, ShapedRetexturedRecipe.STREAM_CODEC));
  // cooking
  public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<SmeltingResultRecipe>> SMELTING = RECIPES.register("smelting", () -> LoadableRecipeSerializer.of(SmeltingResultRecipe.LOADABLE));
  public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<BlastingResultRecipe>> BLASTING = RECIPES.register("blasting", () -> LoadableRecipeSerializer.of(BlastingResultRecipe.LOADABLE));
  public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<SmokingResultRecipe>> SMOKING = RECIPES.register("smoking", () -> LoadableRecipeSerializer.of(SmokingResultRecipe.LOADABLE));
  public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<CampfireResultRecipe>> CAMPFIRE = RECIPES.register("campfire", () -> LoadableRecipeSerializer.of(CampfireResultRecipe.LOADABLE));

  // ingredients
  public static final DeferredHolder<IngredientType<?>,IngredientType<PotionIngredient>> POTION_INGREDIENT = INGREDIENT_TYPES.register("potion", () -> new IngredientType<>(PotionIngredient.CODEC, PotionIngredient.STREAM_CODEC));
  public static final DeferredHolder<IngredientType<?>,IngredientType<PotionDisplayIngredient>> POTION_DISPLAY_INGREDIENT = INGREDIENT_TYPES.register("potion_display", () -> new IngredientType<>(PotionDisplayIngredient.CODEC, PotionDisplayIngredient.STREAM_CODEC));
  public static final DeferredHolder<IngredientType<?>,IngredientType<FluidContainerIngredient>> FLUID_CONTAINER_INGREDIENT = INGREDIENT_TYPES.register("fluid_container", () -> new IngredientType<>(FluidContainerIngredient.CODEC, FluidContainerIngredient.STREAM_CODEC));

  // fluid ingredients
  public static final DeferredHolder<FluidIngredientType<?>,FluidIngredientType<PotionFluidIngredient>> POTION_FLUID_INGREDIENT = FLUID_INGREDIENT_TYPES.register("potion", () -> new FluidIngredientType<>(PotionFluidIngredient.CODEC, PotionFluidIngredient.STREAM_CODEC));
}
