package slimeknights.mantle.recipe;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.ingredient.FluidContainerIngredient;
import slimeknights.mantle.recipe.ingredient.PotionDisplayIngredient;
import slimeknights.mantle.recipe.ingredient.PotionIngredient;

/** Registers Mantle custom ingredient types for NeoForge 1.21+. */
public final class MantleIngredientTypes {
  private static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
      DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Mantle.modId);

  private MantleIngredientTypes() {}

  public static void init(IEventBus bus) {
    INGREDIENT_TYPES.register(bus);
  }

  public static final DeferredHolder<IngredientType<?>, IngredientType<FluidContainerIngredient>> FLUID_CONTAINER =
      INGREDIENT_TYPES.register("fluid_container", () -> FluidContainerIngredient.TYPE);

  public static final DeferredHolder<IngredientType<?>, IngredientType<PotionIngredient>> POTION =
      INGREDIENT_TYPES.register("potion", () -> PotionIngredient.TYPE);

  public static final DeferredHolder<IngredientType<?>, IngredientType<PotionDisplayIngredient>> POTION_DISPLAY =
      INGREDIENT_TYPES.register("potion_display", () -> PotionDisplayIngredient.TYPE);
}

