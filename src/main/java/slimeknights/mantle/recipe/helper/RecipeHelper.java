package slimeknights.mantle.recipe.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import slimeknights.mantle.recipe.IMultiRecipe;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helpers used in creation of recipes
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RecipeHelper {

  /* Recipe manager utils */

  /** Gets all recipes of a given type, working around vanilla's stricter input generic on 1.21. */
  @SuppressWarnings({"rawtypes", "unchecked"})
  private static Stream<Recipe<?>> getRecipeStream(RecipeManager manager, RecipeType<? extends Recipe<?>> type) {
    return ((List<RecipeHolder<?>>)(List<?>)manager.getAllRecipesFor((RecipeType)type)).stream().map(RecipeHolder::value);
  }

  /**
   * Gets a recipe of a specific class type by name from the manager
   * @param manager  Recipe manager
   * @param name     Recipe name
   * @param clazz    Output class
   * @param <C>      Return type
   * @return  Optional of the recipe, or empty if the recipe is missing
   */
  public static <C extends Recipe<?>> Optional<C> getRecipe(RecipeManager manager, ResourceLocation name, Class<C> clazz) {
    return manager.byKey(name).map(RecipeHolder::value).filter(clazz::isInstance).map(clazz::cast);
  }

  /**
   * Gets a list of all recipes from the manager, safely casting to the specified type. Multi Recipes are kept as a single recipe instance
   * @param manager  Recipe manager
   * @param type     Recipe type
   * @param clazz    Preferred recipe class type
   * @param <I>  Inventory interface type
   * @param <T>  Recipe class
   * @param <C>  Return type
   * @return  List of recipes from the manager
   */
  public static List<Recipe<?>> getRecipes(RecipeManager manager, RecipeType<? extends Recipe<?>> type) {
    return getRecipeStream(manager, type).collect(Collectors.toList());
  }

  /**
   * Gets a list of all recipes from the manager, safely casting to the specified type. Multi Recipes are kept as a single recipe instance
   * @param manager  Recipe manager
   * @param type     Recipe type
   * @param clazz    Preferred recipe class type
   * @param <C>  Return type
   * @return  List of recipes from the manager
   */
  public static <C> List<C> getRecipes(RecipeManager manager, RecipeType<? extends Recipe<?>> type, Class<C> clazz) {
    return getRecipeStream(manager, type)
                  .filter(clazz::isInstance)
                  .map(clazz::cast)
                  .collect(Collectors.toList());
  }

  /**
   * Gets a list of recipes for display in a UI list, such as UI buttons. Will be sorted to keep the order the same on both sides, and filtered based on the given predicate and class
   * @param manager  Recipe manager
   * @param type     Recipe type
   * @param clazz    Preferred recipe class type
   * @param filter   Filter for which recipes to add to the list
   * @param <I>  Inventory interface type
   * @param <T>  Recipe class
   * @param <C>  Return type
   * @return  Recipe list
   */
  public static <C extends Recipe<?>> List<C> getUIRecipes(RecipeManager manager, RecipeType<? extends Recipe<?>> type, Class<C> clazz, Predicate<? super C> filter) {
    return getRecipeStream(manager, type)
                  .filter(clazz::isInstance)
                  .map(clazz::cast)
                  .filter(filter)
                  .sorted(Comparator.comparing(recipe -> BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipe.getSerializer()).toString()))
                  .collect(Collectors.toList());
  }

  /**
   * Gets a list of all recipes from the manager, expanding multi recipes. Intended for use in recipe display such as JEI
   * @param <C>  Return type
   * @param access   Registry access instance
   * @param recipes  Stream of recipes
   * @param clazz    Preferred recipe class type
   * @return  List of flattened recipes from the manager
   */
  public static <C> List<C> getJEIRecipes(RegistryAccess access, Stream<? extends Recipe<?>> recipes, Class<C> clazz) {
    return recipes
        .sorted((r1, r2) -> {
          // if one is multi, and the other not, the multi recipe is larger
          boolean m1 = r1 instanceof IMultiRecipe<?>;
          boolean m2 = r2 instanceof IMultiRecipe<?>;
          if (m1 && !m2) return 1;
          if (!m1 && m2) return -1;
          // fall back to recipe ID
          return BuiltInRegistries.RECIPE_SERIALIZER.getKey(r1.getSerializer()).toString().compareTo(BuiltInRegistries.RECIPE_SERIALIZER.getKey(r2.getSerializer()).toString());
        })
        .flatMap((recipe) -> {
          // if its a multi recipe, extract child recipes and stream those
          if (recipe instanceof IMultiRecipe<?>) {
            return ((IMultiRecipe<?>)recipe).getRecipes(access).stream();
          }
          return Stream.of(recipe);
        })
        .filter(clazz::isInstance)
        .map(clazz::cast)
        .collect(Collectors.toList());
  }

  /**
   * Gets a list of all recipes from the manager, expanding multi recipes. Intended for use in recipe display such as JEI
   * @param <C>  Return type
   * @param access   Registry access instance
   * @param manager  Recipe manager
   * @param type     Recipe type
   * @param clazz    Preferred recipe class type
   * @return  List of flattened recipes from the manager
   */
  public static <C> List<C> getJEIRecipes(RegistryAccess access, RecipeManager manager, RecipeType<? extends Recipe<?>> type, Class<C> clazz) {
    return getJEIRecipes(access, getRecipeStream(manager, type), clazz);
  }
}
