package slimeknights.mantle.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Interface for a pair of recipe and name. Like {@link net.minecraft.world.item.crafting.RecipeHolder} but without the strict bound. */
public interface NamedRecipe<T> {
  /** Gets the ID for the recipe. */
  ResourceLocation id();

  /** Gets the recipe instance */
  T recipe();

  /** Named recipe that is itself the recipe */
  interface Self<T> extends NamedRecipe<T> {
    @Override
    @SuppressWarnings("unchecked")
    default T recipe() {
      return (T) this;
    }
  }


  /** Creates a new named recipe instance */
  static <T> NamedRecipe<T> of(ResourceLocation id, T recipe) {
    return new Wrapped<>(id, recipe);
  }

  /** Creates a new named recipe instance from the given recipe holder. */
  static <T extends Recipe<?>> NamedRecipe<T> of(RecipeHolder<T> holder) {
    return new Wrapped<>(holder.id(), holder.value());
  }

  /** Wrapper around an ID and recipe pair. */
  record Wrapped<T>(ResourceLocation id, T recipe) implements NamedRecipe<T> {}
}
