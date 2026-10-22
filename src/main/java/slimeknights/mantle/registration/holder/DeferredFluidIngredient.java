package slimeknights.mantle.registration.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** {@link DeferredHolder} for a {@link FluidIngredientType}. */
public class DeferredFluidIngredient<T extends FluidIngredient> extends DeferredHolder<FluidIngredientType<?>,FluidIngredientType<T>> {
  protected DeferredFluidIngredient(ResourceKey<FluidIngredientType<?>> key) {
    super(key);
  }

  /** Creates a new instance for the given resource location */
  public static <T extends FluidIngredient> DeferredFluidIngredient<T> createType(ResourceLocation key) {
    return createType(ResourceKey.create(NeoForgeRegistries.Keys.FLUID_INGREDIENT_TYPES, key));
  }

  /** Creates a new instance for the given resource key */
  public static <T extends FluidIngredient> DeferredFluidIngredient<T> createType(ResourceKey<FluidIngredientType<?>> key) {
    return new DeferredFluidIngredient<>(key);
  }
}
