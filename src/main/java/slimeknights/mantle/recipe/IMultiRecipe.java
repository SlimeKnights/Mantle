package slimeknights.mantle.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import slimeknights.mantle.recipe.helper.RecipeHelper;

import java.util.List;
import java.util.stream.Stream;

/**
 * This interface is intended to be used on dynamic recipes to return a full list of valid recipes.
 * For vanilla categories, in order to use this interface the recipe must return true for {@link net.minecraft.world.item.crafting.Recipe#isSpecial()} and not match any registered extension to make JEI ignore it.
 * @param <T>  Recipe type for the return
 */
public interface IMultiRecipe<T> {
  /**
   * Gets a list of recipes for display in JEI
   * @return  List of recipes
   * @param access  Registry access instance
   */
  List<T> getRecipes(RegistryAccess access);


  /**
   * Gets a list of all expanded multi-recipes for the given recipe type. Used to expand multi recipes in vanilla categories.
   * For custom categories, usually it's better to call {@link RecipeHelper#getJEIRecipes(RegistryAccess, RecipeManager, RecipeType, Class)} as that includes regular recipes too.
   * @see RecipeHelper#getJEIRecipes(RegistryAccess, RecipeManager, RecipeType, Class)
   */
  @SuppressWarnings("SameParameterValue") // might want it later for other recipe types
  static <I extends Container, T extends Recipe<I>, C> Stream<C> getMultiRecipes(RegistryAccess access, RecipeManager manager, RecipeType<T> type, Class<C> clazz) {
    return manager.byType(type).values().stream().filter(Recipe::isSpecial)
      .flatMap(recipe -> recipe instanceof IMultiRecipe<?> r ? r.getRecipes(access).stream() : Stream.empty())
      .filter(clazz::isInstance).map(clazz::cast);
  }
}
