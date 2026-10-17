package slimeknights.mantle.recipe.crafting.fallback;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/** Common logic between {@link ShapedFallbackRecipe} and {@link ShapelessFallbackRecipe} */
public interface FallbackCraftingRecipe {
  /** Fetches and caches the alternatives. Can use {@link #computeAlternatives(Level, List)}. */
  List<CraftingRecipe> getAlternatives(Level level);

  /** Checks if any alternative matches. */
  default boolean matchesAlternatives(CraftingInput input, Level level) {
    // fail if any alternative matches
    return getAlternatives(level).stream().noneMatch(recipe -> recipe.matches(input, level));
  }

  /** Computes the alternatives for the given list of IDs. */
  static List<CraftingRecipe> computeAlternatives(Level level, List<ResourceLocation> ids) {
    List<CraftingRecipe> alternatives = new ArrayList<>();
    RecipeManager manager = level.getRecipeManager();
    for (ResourceLocation id : ids) {
      RecipeHolder<CraftingRecipe> holder = manager.byKeyTyped(RecipeType.CRAFTING, id);
      if (holder != null) {
        alternatives.add(holder.value());
      }
    }
    return List.copyOf(alternatives);
  }
}
