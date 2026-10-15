package slimeknights.mantle.plugin.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.recipe.NamedRecipe;

import java.util.List;

/** Recipe category containing {@link NamedRecipe}. Automatically implements recipe ID methods. */
@SuppressWarnings("unused")  // API
public abstract class NamedRecipeCategory<T> extends AbstractRecipeCategory<NamedRecipe<T>> {
  public NamedRecipeCategory(RecipeType<NamedRecipe<T>> recipeType, Component title, IDrawable icon, int width, int height) {
    super(recipeType, title, icon, width, height);
  }

  @Override
  public ResourceLocation getRegistryName(NamedRecipe<T> recipe) {
    return recipe.id();
  }

  @Override
  public List<ResourceLocation> getSourceRecipeIds(NamedRecipe<T> recipe) {
    return recipe.sourceIds();
  }
}
