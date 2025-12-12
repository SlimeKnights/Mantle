package slimeknights.mantle.recipe.crafting;

import lombok.RequiredArgsConstructor;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.neoforge.common.conditions.ICondition;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Builder for a shaped recipe with fallbacks */
@SuppressWarnings("unused")
@RequiredArgsConstructor(staticName = "fallback")
public class ShapedFallbackRecipeBuilder {
  private final ShapedRecipeBuilder base;
  private final List<ResourceLocation> alternatives = new ArrayList<>();

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

  /**
   * Builds the recipe using the output as the name
   * @param output  Recipe output
   */
  public void build(RecipeOutput output) {
    base.save(new Wrapper(output, List.copyOf(alternatives)));
  }

  /**
   * Builds the recipe using the given ID
   * @param output    Recipe output
   * @param id        Recipe ID
   */
  public void build(RecipeOutput output, ResourceLocation id) {
    base.save(new Wrapper(output, List.copyOf(alternatives)), ResourceKey.create(Registries.RECIPE, id));
  }

  private record Wrapper(RecipeOutput parent, List<ResourceLocation> alternatives) implements RecipeOutput {
    @Override
    public Advancement.Builder advancement() {
      return parent.advancement();
    }

    @Override
    public void accept(ResourceKey<Recipe<?>> id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
      if (!(recipe instanceof ShapedRecipe shaped)) {
        throw new IllegalStateException("Expected shaped recipe from ShapedRecipeBuilder, got " + recipe);
      }
      parent.accept(id, new ShapedFallbackRecipe(shaped, alternatives), advancement, conditions);
    }
  }
}
