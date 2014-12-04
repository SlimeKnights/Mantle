package mantle.utils;

import java.util.List;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.common.ChestGenHooks;

@SuppressWarnings("unchecked")
public class RecipeRemover
{
    public static void removeShapedRecipes (List<ItemStack> removelist)
    {
        for (ItemStack stack : removelist)
            removeShapedRecipe(stack);
    }

    public static void removeAnyRecipe (ItemStack resultItem)
    {
        List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
        for (int i = 0; i < recipes.size(); i++)
        {
            IRecipe tmpRecipe = recipes.get(i);
            ItemStack recipeResult = tmpRecipe.getRecipeOutput();
            if (ItemStack.areItemStacksEqual(resultItem, recipeResult))
            {
                recipes.remove(i--);
            }
        }
    }

    public static void removeShapedRecipe (ItemStack resultItem)
    {
        List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
        for (int i = 0; i < recipes.size(); i++)
        {
            IRecipe tmpRecipe = recipes.get(i);
            if (tmpRecipe instanceof ShapedRecipes)
            {
                ShapedRecipes recipe = (ShapedRecipes) tmpRecipe;
                ItemStack recipeResult = recipe.getRecipeOutput();

                if (ItemStack.areItemStacksEqual(resultItem, recipeResult))
                {
                    recipes.remove(i--);
                }
            }
        }
    }

    public static void removeShapelessRecipe (ItemStack resultItem)
    {
        List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
        for (int i = 0; i < recipes.size(); i++)
        {
            IRecipe tmpRecipe = recipes.get(i);
            if (tmpRecipe instanceof ShapelessRecipes)
            {
                ShapelessRecipes recipe = (ShapelessRecipes) tmpRecipe;
                ItemStack recipeResult = recipe.getRecipeOutput();

                if (ItemStack.areItemStacksEqual(resultItem, recipeResult))
                {
                    recipes.remove(i--);
                }
            }
        }
    }

    public static void removeFurnaceRecipe (ItemStack resultItem)
    {
        Map<ItemStack, ItemStack> recipes = FurnaceRecipes.instance().getSmeltingList();
        recipes.remove(resultItem);
    }

    public static void removeFurnaceRecipe (Item i, int metadata)
    {
        removeFurnaceRecipe(new ItemStack(i, 1, metadata));
    }

    public static void removeFurnaceRecipe (Item i)
    {
        removeFurnaceRecipe(new ItemStack(i, 1, 32767));
    }

    //removes from all vanilla worldgen chests :D
    public static void removeFromChests (ItemStack resultItem)
    {
        ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).removeItem(resultItem);
        ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).removeItem(resultItem);
        ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).removeItem(resultItem);
        ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).removeItem(resultItem);
        ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).removeItem(resultItem);
        ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).removeItem(resultItem);
        ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_DISPENSER).removeItem(resultItem);
        ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).removeItem(resultItem);
        ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).removeItem(resultItem);
        ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_LIBRARY).removeItem(resultItem);

    }
}
