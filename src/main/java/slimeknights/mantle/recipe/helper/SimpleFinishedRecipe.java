package slimeknights.mantle.recipe.helper;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Helper for datagen with {@link SimpleRecipeSerializer}.
 *
 * <p>In 1.21+ datagen, recipes are emitted through {@link RecipeOutput} rather than {@code FinishedRecipe}.
 */
@SuppressWarnings("unused")
public final class SimpleFinishedRecipe {
  private SimpleFinishedRecipe() {}

  /** Emits a recipe using the given serializer's constructor. */
  public static <T extends Recipe<?>> void accept(RecipeOutput output, ResourceLocation id, SimpleRecipeSerializer<T> serializer) {
    output.accept(ResourceKey.create(Registries.RECIPE, id), serializer.constructor().apply(id), null);
  }

}
