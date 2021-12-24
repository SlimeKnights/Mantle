package slimeknights.mantle.recipe;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ObjectHolder;
import slimeknights.mantle.Mantle;

import static slimeknights.mantle.registration.RegistrationHelper.injected;

/**
 * All recipe serializers registered under Mantles name
 */
@ObjectHolder(Mantle.modId)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MantleRecipeSerializers {
  public static final RecipeSerializer<?> CRAFTING_SHAPED_FALLBACK = injected();
  public static final RecipeSerializer<?> CRAFTING_SHAPED_RETEXTURED = injected();
}
