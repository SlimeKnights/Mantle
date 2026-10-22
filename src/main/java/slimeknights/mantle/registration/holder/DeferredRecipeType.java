package slimeknights.mantle.registration.holder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

/** {@link DeferredHolder} for {@link RecipeType}. Requires using {@link RecipeType} for the supplier. */
public class DeferredRecipeType<T extends Recipe<?>> extends DeferredHolder<RecipeType<?>,RecipeType<T>> {
  protected DeferredRecipeType(ResourceKey<RecipeType<?>> key) {
    super(key);
  }

  /** Creates a new instance for the given resource location */
  public static <T extends Recipe<?>> DeferredRecipeType<T> createType(ResourceLocation key) {
    return createType(ResourceKey.create(Registries.RECIPE_TYPE, key));
  }

  /** Creates a new instance for the given resource key */
  public static <T extends Recipe<?>> DeferredRecipeType<T> createType(ResourceKey<RecipeType<?>> key) {
    return new DeferredRecipeType<>(key);
  }
}
