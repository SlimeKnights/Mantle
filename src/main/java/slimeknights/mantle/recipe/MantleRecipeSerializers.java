package slimeknights.mantle.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.Mantle;

import static slimeknights.mantle.registration.RegistrationHelper.injected;

/** @deprecated use {@link MantleRecipes} */
@Deprecated(forRemoval = true)
public class MantleRecipeSerializers {
  private MantleRecipeSerializers() {}

  /** @deprecated use {@link MantleRecipes#CRAFTING_SHAPED_FALLBACK} */
  @Deprecated(forRemoval = true)
  public static final RecipeSerializer<?> CRAFTING_SHAPED_FALLBACK = injected();
  /** @deprecated use {@link MantleRecipes#CRAFTING_SHAPED_RETEXTURED} */
  @Deprecated(forRemoval = true)
  public static final RecipeSerializer<?> CRAFTING_SHAPED_RETEXTURED = injected();
}
