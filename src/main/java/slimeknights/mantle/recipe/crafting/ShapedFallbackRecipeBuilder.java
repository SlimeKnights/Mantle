package slimeknights.mantle.recipe.crafting;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Builder for a shaped recipe with fallbacks */
@SuppressWarnings("unused")  // API
public class ShapedFallbackRecipeBuilder extends ShapedExtensionBuilder<ShapedFallbackRecipeBuilder> {
  private final List<ResourceLocation> alternatives = new ArrayList<>();

  protected ShapedFallbackRecipeBuilder(ItemStack result) {
    super(result);
  }

  /** Creates a builder for the given stack */
  public static ShapedFallbackRecipeBuilder shaped(ItemStack result) {
    return new ShapedFallbackRecipeBuilder(result);
  }

  /** Creates a builder for the given item and count */
  public static ShapedFallbackRecipeBuilder shaped(ItemLike result, int count) {
    return shaped(new ItemStack(result, count));
  }

  /** Creates a builder for the given item */
  public static ShapedFallbackRecipeBuilder shaped(ItemLike result) {
    return shaped(result, 1);
  }

  /**
   * Adds a single alternative to this recipe. Any matching alternative causes this recipe to fail
   * @param location  Alternative
   * @return  Builder instance
   */
  public ShapedFallbackRecipeBuilder addAlternative(ResourceLocation location) {
    this.alternatives.add(location);
    return this;
  }

  /**
   * Adds a list of alternatives to this recipe. Any matching alternative causes this recipe to fail
   * @param locations  Alternative list
   * @return  Builder instance
   */
  public ShapedFallbackRecipeBuilder addAlternatives(Collection<ResourceLocation> locations) {
    this.alternatives.addAll(locations);
    return this;
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    output.accept(id,
      new ShapedFallbackRecipe(group, getBookCategory(), getPattern(), result, showNotification, alternatives),
      buildAdvancement(output, id, category.getFolderName())
    );
  }
}
