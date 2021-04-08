package slimeknights.mantle.recipe;

import net.minecraft.recipe.Recipe;
import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * This class simply exists because every recipe serializer has to extend the forge registry entry and implement the interface. Easier to just extend this class
 * @param <T> Recipe type
 */
public abstract class RecipeSerializer<T extends Recipe<?>> extends ForgeRegistryEntry<net.minecraft.recipe.RecipeSerializer<?>> implements net.minecraft.recipe.RecipeSerializer<T> {}
