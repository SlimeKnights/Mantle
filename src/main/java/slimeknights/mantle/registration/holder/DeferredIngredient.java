package slimeknights.mantle.registration.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** {@link DeferredHolder} for a {@link IngredientType}. */
public class DeferredIngredient<T extends ICustomIngredient> extends DeferredHolder<IngredientType<?>,IngredientType<T>> {
  protected DeferredIngredient(ResourceKey<IngredientType<?>> key) {
    super(key);
  }

  /** Creates a new instance for the given resource location */
  public static <T extends ICustomIngredient> DeferredIngredient<T> createType(ResourceLocation key) {
    return createType(ResourceKey.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, key));
  }

  /** Creates a new instance for the given resource key */
  public static <T extends ICustomIngredient> DeferredIngredient<T> createType(ResourceKey<IngredientType<?>> key) {
    return new DeferredIngredient<>(key);
  }
}
