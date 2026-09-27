package slimeknights.mantle.recipe;


import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import slimeknights.mantle.Mantle;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Interface to use on custom recipe types for display in JEI. Unlike {@link MultiRecipeHolder}, does not require the recipe to implement {@link net.minecraft.world.item.crafting.Recipe}.
 * Recipes implementing this interface will not be included in the list of recipes in JEI, and will automatically be sorted after recips that do not use this interface.
 * @see MultiRecipeHolder
 */
public interface MultiNamedRecipe<T> {
  /** Comparator to sort recipe holders, first sorting multi recipes last, then sorting by ID. */
  Comparator<RecipeHolder<?>> RECIPE_COMPARATOR = Comparator.comparing((RecipeHolder<?> r) -> r.value() instanceof MultiNamedRecipe ? 1 : 0).thenComparing(RecipeHolder::id);

  /**
   * Gets a list of recipes for display in JEI.
   * @param id        ID of the base recipe. IDs of returned recipes should be derived from this ID while remaining unique for the sake of bookmarks.
   * @param provider  Registry access.
   * @return  List of recipes to display.
   */
  List<NamedRecipe<T>> getRecipes(ResourceLocation id, HolderLookup.Provider provider);


  /**
   * Gets a stream of recipes without names. More efficient for contexts that do not need recipe IDs such as book display.
   * @param provider     Registry provider
   * @param manager      Recipe manager
   * @param type         Base recipe type to filter
   * @param recipeClass  Desired final recipe class, used to filter expanded recipes.
   * @return  Stream of recipes of the given type.
   * @param <T>  Result recipe type.
   */
  static <T> Stream<T> streamRecipes(HolderLookup.Provider provider, RecipeManager manager, RecipeType<?> type, Class<T> recipeClass) {
    return manager.byType(type).stream().sorted(RECIPE_COMPARATOR).flatMap(base -> {
      if (base.value() instanceof MultiNamedRecipe<?> multi) {
        ResourceLocation id = base.id();
        try {
         return multi.getRecipes(id, provider).stream().map(NamedRecipe::recipe);
        } catch (Exception e) {
          Mantle.logger.error("Failed to fetch JEI recipes for multi recipe {} ({})", id, multi, e);
          return Stream.empty();
        }
      }
      return Stream.of(base.value());
    }).filter(recipeClass::isInstance).map(recipeClass::cast);
  }

  /**
   * Gets a stream of recipes with names. Meant to collect the recipes to show in JEI.
   * @param provider     Registry provider
   * @param manager      Recipe manager
   * @param type         Base recipe type to filter
   * @param recipeClass  Desired final recipe class, used to filter expanded recipes.
   * @return  Stream of recipes of the given type.
   * @param <T>  Result recipe type.
   * @see #streamRecipes(Provider, RecipeManager, RecipeType, Class)
   * @see #getNamedRecipes(Provider, RecipeManager, RecipeType, Class) 
   */
  @SuppressWarnings("unchecked")
  static <T> Stream<NamedRecipe<T>> streamNamedRecipes(HolderLookup.Provider provider, RecipeManager manager, RecipeType<?> type, Class<T> recipeClass) {
    return manager.byType(type).stream().sorted(RECIPE_COMPARATOR).flatMap(base -> {
      ResourceLocation id = base.id();
      Recipe<?> recipe = base.value();
      if (recipe instanceof MultiNamedRecipe<?> multi) {
        try {
          return multi.getRecipes(id, provider).stream();
        } catch (Exception e) {
          Mantle.logger.error("Failed to fetch JEI recipes for multi recipe {} ({})", id, recipe, e);
          return Stream.empty();
        }
      }
      return Stream.of(NamedRecipe.of(id, recipe));
    }).filter(r -> recipeClass.isInstance(r.recipe())).map(r -> (NamedRecipe<T>) r);
  }

  /**
   * Gets a stream of recipes with names. Meant to collect the recipes to show in JEI.
   * @param provider     Registry provider
   * @param manager      Recipe manager
   * @param type         Base recipe type to filter
   * @param recipeClass  Desired final recipe class, used to filter expanded recipes.
   * @return  Stream of recipes of the given type.
   * @param <T>  Result recipe type.
   * @see #streamRecipes(Provider, RecipeManager, RecipeType, Class) 
   * @see #streamNamedRecipes(Provider, RecipeManager, RecipeType, Class)
   */
  static <T> List<NamedRecipe<T>> getNamedRecipes(HolderLookup.Provider provider, RecipeManager manager, RecipeType<?> type, Class<T> recipeClass) {
    return streamNamedRecipes(provider, manager, type, recipeClass).toList();
  }
}
