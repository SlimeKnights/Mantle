package slimeknights.mantle.recipe;

import java.util.List;

/**
 * This interface is intended to be used on dynamic recipes to return a full list of valid recipes.
 * @param <T>  Recipe type for the return
 */
public interface IMultiRecipe<T> {
  /**
   * Gets a list of recipes for display in JEI
   * @return  List of recipes
   */
  List<T> getRecipes();
}
