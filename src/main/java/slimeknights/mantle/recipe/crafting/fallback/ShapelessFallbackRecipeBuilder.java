package slimeknights.mantle.recipe.crafting.fallback;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.recipe.crafting.ShapelessExtensionBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Builder for a shapeless recipe with fallbacks. If any fallback matches then the base recipe will not.
 * @see ShapelessFallbackRecipe
 */
@SuppressWarnings("unused")  // API
public class ShapelessFallbackRecipeBuilder extends ShapelessExtensionBuilder<ShapelessFallbackRecipeBuilder> {
  private final List<ResourceLocation> alternatives = new ArrayList<>();

  protected ShapelessFallbackRecipeBuilder(ItemStack result) {
    super(result);
  }

  /** Creates a builder for the given stack */
  public static ShapelessFallbackRecipeBuilder shapeless(ItemStack result) {
    return new ShapelessFallbackRecipeBuilder(result);
  }

  /** Creates a builder for the given item and count */
  public static ShapelessFallbackRecipeBuilder shapeless(ItemLike result, int count) {
    return shapeless(new ItemStack(result, count));
  }

  /** Creates a builder for the given item */
  public static ShapelessFallbackRecipeBuilder shapeless(ItemLike result) {
    return shapeless(result, 1);
  }

  /**
   * Adds a single alternative to this recipe. Any matching alternative causes this recipe to fail
   * @param location  Alternative
   * @return  Builder instance
   */
  public ShapelessFallbackRecipeBuilder addAlternative(ResourceLocation location) {
    this.alternatives.add(location);
    return this;
  }

  /**
   * Adds a list of alternatives to this recipe. Any matching alternative causes this recipe to fail
   * @param locations  Alternative list
   * @return  Builder instance
   */
  public ShapelessFallbackRecipeBuilder addAlternatives(Collection<ResourceLocation> locations) {
    this.alternatives.addAll(locations);
    return this;
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    output.accept(id,
      new ShapelessFallbackRecipe(group, getBookCategory(), result, ingredients, alternatives),
      buildAdvancement(output, id, category.getFolderName())
    );
  }
}
