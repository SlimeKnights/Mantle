package slimeknights.mantle.registration.holder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

/** {@link DeferredHolder} for {@link RecipeSerializer}. Requires using {@link RecipeSerializer} for the supplier. */
public class DeferredRecipeSerializer<T extends Recipe<?>> extends DeferredHolder<RecipeSerializer<?>,RecipeSerializer<T>> {
  protected DeferredRecipeSerializer(ResourceKey<RecipeSerializer<?>> key) {
    super(key);
  }

  /** Creates a new instance for the given resource location */
  public static <T extends Recipe<?>> DeferredRecipeSerializer<T> createSerializer(ResourceLocation key) {
    return createSerializer(ResourceKey.create(Registries.RECIPE_SERIALIZER, key));
  }

  /** Creates a new instance for the given resource key */
  public static <T extends Recipe<?>> DeferredRecipeSerializer<T> createSerializer(ResourceKey<RecipeSerializer<?>> key) {
    return new DeferredRecipeSerializer<>(key);
  }
}
