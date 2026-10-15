package slimeknights.mantle.recipe.data.mock;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import slimeknights.mantle.data.GenericDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Alternative to {@link net.minecraft.data.recipes.RecipeProvider} which allows serializing recipes from raw JSON via {@link MockRecipe}.
 * Used when the mod adding the recipe is not present at datagen time, for optional compatability.
 */
public abstract class MockRecipeProvider extends GenericDataProvider {
  private final CompletableFuture<HolderLookup.Provider> registries;
  public MockRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
    super(output, Target.DATA_PACK, Registries.elementsDirPath(Registries.RECIPE));
    this.registries = registries;
  }

  /** Adds all mock recipes */
  protected abstract void buildRecipes(MockRecipeConsumer consumer, HolderLookup.Provider registries);

  @Override
  public final CompletableFuture<?> run(CachedOutput output) {
    return this.registries.thenCompose(provider -> this.run(output, provider));
  }

  /** Runs with the registry provider */
  protected CompletableFuture<?> run(CachedOutput cachedOutput, HolderLookup.Provider registries) {
    List<MockRecipe> recipes = new ArrayList<>();
    MockRecipeConsumer consumer = recipes::add;
    buildRecipes(consumer, registries);
    MockRecipe.Serializer serializer = new MockRecipe.Serializer(registries);
    return allOf(recipes.stream().map(recipe -> saveJson(cachedOutput, recipe.id(), recipe.serializeRecipe(serializer))));
  }
}
