package slimeknights.mantle.registration.deferred;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeknights.mantle.registration.holder.DeferredRecipeType;

import java.util.function.Function;
import java.util.function.Supplier;

/** {@link DeferredRegister} for {@link RecipeType}. Simplifies usage of {@link RecipeType#simple(ResourceLocation)}. */
@SuppressWarnings("unused")
public class RecipeTypeDeferredRegister extends DeferredRegister<RecipeType<?>> {
  public RecipeTypeDeferredRegister(String namespace) {
    super(Registries.RECIPE_TYPE, namespace);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected <I extends RecipeType<?>> DeferredHolder<RecipeType<?>, I> createHolder(ResourceKey<? extends Registry<RecipeType<?>>> registryKey, ResourceLocation key) {
    // this should be fine as IngredientType is a record
    return (DeferredHolder<RecipeType<?>, I>) DeferredRecipeType.createType(ResourceKey.create(registryKey, key));
  }

  /** @deprecated use {@link #registerType(String)} */
  @Deprecated
  @Override
  public <I extends RecipeType<?>> DeferredHolder<RecipeType<?>, I> register(String name, Function<ResourceLocation, ? extends I> func) {
    return super.register(name, func);
  }

  /** @deprecated use {@link #registerType(String)} */
  @Deprecated
  @Override
  public <I extends RecipeType<?>> DeferredHolder<RecipeType<?>, I> register(String name, Supplier<? extends I> sup) {
    return super.register(name, sup);
  }

  /** Registers a recipe type for the given name */
  public <T extends Recipe<?>> DeferredRecipeType<T> registerType(String name) {
    return (DeferredRecipeType<T>) register(name, RecipeType::<T>simple);
  }
}
