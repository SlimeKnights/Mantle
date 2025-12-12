package slimeknights.mantle.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;

/** @deprecated use {@link MantleRecipes} */
@Deprecated(forRemoval = true)
public class MantleRecipeSerializers {
  private MantleRecipeSerializers() {}

  /** @deprecated use {@link MantleRecipes#CRAFTING_SHAPED_FALLBACK} */
  @Deprecated(forRemoval = true)
  public static RecipeSerializer<?> getCraftingShapedFallback() {
    return MantleRecipes.CRAFTING_SHAPED_FALLBACK.get();
  }
  
  /** @deprecated use {@link MantleRecipes#CRAFTING_SHAPED_RETEXTURED} */
  @Deprecated(forRemoval = true)
  public static RecipeSerializer<?> getCraftingShapedRetextured() {
    return MantleRecipes.CRAFTING_SHAPED_RETEXTURED.get();
  }
}
