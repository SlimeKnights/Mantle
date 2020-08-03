package slimeknights.mantle.recipe;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.registries.ObjectHolder;
import slimeknights.mantle.Mantle;

import static slimeknights.mantle.registration.RegistrationHelper.injected;

/**
 * All recipe serializers registered under Mantles name
 */
@ObjectHolder(Mantle.modId)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MantleRecipeSerializers {
  public static final IRecipeSerializer<?> CRAFTING_SHAPED_FALLBACK = injected();
}
