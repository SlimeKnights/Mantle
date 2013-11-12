package mantle.compat.minecraft.misc

import scala.collection.JavaConversions._
import java.util
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.CraftingManager
import net.minecraft.item.crafting.IRecipe
import net.minecraft.item.crafting.ShapedRecipes
import net.minecraft.item.crafting.ShapelessRecipes

/**
 * Utility to remove recipes from the vanilla crafting handler
 *
 * @author mDiyo
 * @author Sunstrike
 */
object RecipeRemover {

  /**
   * Removes recipes of any type that return the given itemstack
   *
   * @param resultItem The result to find and remove
   */
  def removeAnyRecipe(resultItem: ItemStack) {
    val recipes: util.List[IRecipe] = CraftingManager.getInstance.getRecipeList.asInstanceOf
    for (recipe: IRecipe <- recipes) {
      val res = recipe.getRecipeOutput
      if (ItemStack.areItemStacksEqual(resultItem, res)) recipes.remove(recipe)
    }
  }

  /**
   * Removes shaped recipes that return the given itemstack
   *
   * @param resultItem The result to find and remove
   */
  def removeShapedRecipe(resultItem: ItemStack) {
    val recipes: util.List[IRecipe] = CraftingManager.getInstance.getRecipeList.asInstanceOf
    for (recipe: IRecipe <- recipes) {
      recipe match {
        case rec:ShapedRecipes =>
          if (ItemStack.areItemStacksEqual(rec.getRecipeOutput, resultItem)) recipes.remove(recipe)
      }
    }
  }

  /**
   * Removes shapeless recipes that return the given itemstack
   *
   * @param resultItem The result to find and remove
   */
  def removeShapelessRecipe(resultItem: ItemStack) {
    val recipes: util.List[IRecipe] = CraftingManager.getInstance.getRecipeList.asInstanceOf
    for (recipe: IRecipe <- recipes) {
      recipe match {
        case rec:ShapelessRecipes =>
          if (ItemStack.areItemStacksEqual(rec.getRecipeOutput, resultItem)) recipes.remove(recipe)
      }
    }
  }
}
