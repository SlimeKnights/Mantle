package mantle.lib.client;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

import mantle.books.client.BookImage;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class MantleClientRegistry
{
    public static Map<String, ItemStack> manualIcons = Maps.newHashMap();
    public static Map<String, ItemStack[]> recipeIcons = Maps.newHashMap();
    public static HashMap<String, BookImage> imageCache = Maps.newHashMap();

    public static ItemStack defaultStack = new ItemStack(Items.iron_ingot);

    public static BookImage getBookImageFromCache (String s)
    {
        return imageCache.get(s);
    }

    public static void registerManualIcon (String name, ItemStack stack)
    {
        manualIcons.put(name, stack);
    }

    public static ItemStack getManualIcon (String textContent)
    {
        ItemStack stack = manualIcons.get(textContent);
        if (stack != null)
            return stack;
        return defaultStack;
    }

    public static void registerManualSmallRecipe (String name, ItemStack output, ItemStack... stacks)
    {
        ItemStack[] recipe = new ItemStack[5];
        recipe[0] = output;
        System.arraycopy(stacks, 0, recipe, 1, 4);
        recipeIcons.put(name, recipe);
    }

    public static void registerManualLargeRecipe (String name, ItemStack output, ItemStack... stacks)
    {
        ItemStack[] recipe = new ItemStack[10];
        recipe[0] = output;
        System.arraycopy(stacks, 0, recipe, 1, 9);
        recipeIcons.put(name, recipe);
    }

    public static void registerManualFurnaceRecipe (String name, ItemStack output, ItemStack input)
    {
        ItemStack[] recipe = new ItemStack[2];
        recipe[0] = output;
        recipe[1] = input;
        recipeIcons.put(name, recipe);
    }

    public static ItemStack[] getRecipeIcons (String recipeName)
    {
        return recipeIcons.get(recipeName);
    }

}
