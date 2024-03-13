package slimeknights.mantle.plugin.jei;

import com.google.common.collect.Streams;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.crafting.ShapedRetexturedRecipe;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * JEI crafting extension to properly show, animate, and focus {@link ShapedRetexturedRecipe} instances
 */
public class RetexturableRecipeExtension implements ICraftingCategoryExtension {
  /** Actual recipe instance */
  private final ShapedRetexturedRecipe recipe;
  /** List of all textured variants, fallback for JEI display */
  private final List<ItemStack> displayOutputs;
  /** Ingredient indexes of all texture slots */
  private final int[] textureSlots;

  RetexturableRecipeExtension(ShapedRetexturedRecipe recipe) {
    this.recipe = recipe;

    // set the output to display all variants from the texture ingredient
    Ingredient texture = recipe.getTexture();
    // fetch all stacks from the ingredient, note any variants that are not blocks will get a blank look
    List<ItemStack> displayOutputs = Arrays.stream(texture.getItems())
                                           .map(stack -> recipe.getRecipeOutput(stack.getItem()))
                                           .toList();
    // empty display means the tag found nothing, so just use the original output
    this.displayOutputs = displayOutputs.isEmpty() ? List.of(this.recipe.getResultItem()) : displayOutputs;

    // find out which inputs match the texture, we will need to use those for the focus link
    List<Ingredient> inputs = recipe.getIngredients();
    this.textureSlots = IntStream.range(0, inputs.size()).filter(i -> ingredientsMatch(texture, inputs.get(i))).toArray();
  }

  /** Checks if two ingredients match based on their display items */
  private static boolean ingredientsMatch(Ingredient left, Ingredient right) {
    ItemStack[] leftStacks = left.getItems();
    ItemStack[] rightStacks = right.getItems();
    if (leftStacks.length != rightStacks.length) {
      return false;
    }
    for (int i = 0; i < leftStacks.length; i++) {
      if (!ItemStack.isSameItemSameTags(leftStacks[i], rightStacks[i])) {
        return false;
      }
    }
    return true;
  }

  @Override
  public ResourceLocation getRegistryName() {
    return this.recipe.getId();
  }

  @Override
  public int getWidth() {
    return recipe.getRecipeWidth();
  }

  @Override
  public int getHeight() {
    return recipe.getRecipeHeight();
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
//    guiItemStacks.addTooltipCallback(this);
    // we need the blank version for the sake of recipe lookup due to the subtype interpreter making it not the same
    builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(recipe.getResultItem());

    // add the itemstacks to the grid
    List<List<ItemStack>> inputStacks = recipe.getIngredients().stream().map(ingredient -> List.of(ingredient.getItems())).toList();
    int width = recipe.getWidth();
    int height = recipe.getHeight();
    List<IRecipeSlotBuilder> inputs = craftingGridHelper.createAndSetInputs(builder, VanillaTypes.ITEM_STACK, inputStacks, recipe.getWidth(), recipe.getHeight());
    IRecipeSlotBuilder output = craftingGridHelper.createAndSetOutputs(builder, displayOutputs);
    if (inputs.size() != 9) {
      Mantle.logger.error("Failed to create focus link for {} as the layout {} is not 3x3", recipe.getId(), builder.getClass().getName());
    } else {
      // link the output to all inputs that match the texture
      builder.createFocusLink(Streams.concat(Stream.of(output), Arrays.stream(textureSlots).mapToObj(i -> inputs.get(getCraftingIndex(i, width, height)))).toArray(IRecipeSlotBuilder[]::new));
    }
  }

  /** Borrowed from {@link ICraftingGridHelper} implementation. Ideally I'd call it from the API, but the API lacks all the information I need for that. */
  private static int getCraftingIndex(int i, int width, int height) {
    int index;
    if (width == 1) {
      if (height == 3) {
        index = (i * 3) + 1;
      } else if (height == 2) {
        index = (i * 3) + 1;
      } else {
        index = 4;
      }
    } else if (height == 1) {
      index = i + 3;
    } else if (width == 2) {
      index = i;
      if (i > 1) {
        index++;
        if (i > 3) {
          index++;
        }
      }
    } else if (height == 2) {
      index = i + 3;
    } else {
      index = i;
    }
    return index;
  }
}
