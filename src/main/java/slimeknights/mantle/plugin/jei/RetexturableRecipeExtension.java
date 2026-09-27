package slimeknights.mantle.plugin.jei;

import com.google.common.collect.Streams;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.crafting.ShapedRetexturedRecipe;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * JEI crafting extension to properly show, animate, and focus {@link ShapedRetexturedRecipe} instances
 */
public class RetexturableRecipeExtension implements ICraftingCategoryExtension<ShapedRetexturedRecipe> {
  @Override
  public int getWidth(RecipeHolder<ShapedRetexturedRecipe> recipeHolder) {
    return recipeHolder.value().getWidth();
  }

  @Override
  public int getHeight(RecipeHolder<ShapedRetexturedRecipe> recipeHolder) {
    return recipeHolder.value().getHeight();
  }

  @Override
  public void setRecipe(RecipeHolder<ShapedRetexturedRecipe> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
    ShapedRetexturedRecipe recipe = recipeHolder.value();

    // we need the blank version for the sake of recipe lookup due to the subtype interpreter making it not the same
    builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(recipe.getPlainOutput());

    // add the itemstacks to the grid
    List<List<ItemStack>> inputStacks = recipe.getIngredients().stream().map(ingredient -> List.of(ingredient.getItems())).toList();
    int width = recipe.getWidth();
    int height = recipe.getHeight();
    List<IRecipeSlotBuilder> inputs = craftingGridHelper.createAndSetInputs(builder, VanillaTypes.ITEM_STACK, inputStacks, recipe.getWidth(), recipe.getHeight());
    IRecipeSlotBuilder output = craftingGridHelper.createAndSetOutputs(builder, recipe.getDisplayOutputs());
    if (inputs.size() != 9) {
      Mantle.logger.error("Failed to create focus link for {} as the layout {} is not 3x3", recipeHolder.id(), builder.getClass().getName());
    } else {
      // link the output to all inputs that match the texture
      builder.createFocusLink(Streams.concat(Stream.of(output), Arrays.stream(recipe.getTextureSlots()).mapToObj(i -> inputs.get(MantleJEIConstants.getCraftingIndex(i, width, height)))).toArray(IRecipeSlotBuilder[]::new));
    }
  }
}
