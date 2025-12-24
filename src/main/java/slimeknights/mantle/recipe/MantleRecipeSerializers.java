package slimeknights.mantle.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ObjectHolder;
import slimeknights.mantle.Mantle;

import static slimeknights.mantle.registration.RegistrationHelper.injected;

/** @deprecated use {@link MantleRecipes} */
@Deprecated(forRemoval = true)
public class MantleRecipeSerializers {
  private MantleRecipeSerializers() {}

  /** @deprecated use {@link MantleRecipes#CRAFTING_SHAPED_FALLBACK} */
  @Deprecated(forRemoval = true)
  @ObjectHolder(registryName = "minecraft:recipe_serializer", value = Mantle.modId+":crafting_shaped_fallback")
  public static final RecipeSerializer<?> CRAFTING_SHAPED_FALLBACK = injected();
  /** @deprecated use {@link MantleRecipes#CRAFTING_SHAPED_RETEXTURED} */
  @Deprecated(forRemoval = true)
  @ObjectHolder(registryName = "minecraft:recipe_serializer", value = Mantle.modId+":crafting_shaped_retextured")
  public static final RecipeSerializer<?> CRAFTING_SHAPED_RETEXTURED = injected();
}
