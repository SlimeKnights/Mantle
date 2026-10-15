package slimeknights.mantle.recipe.data.mock;

import com.google.gson.JsonElement;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.PathProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import slimeknights.mantle.data.GenericDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Alternative to {@link net.minecraft.data.recipes.RecipeProvider} which allows serializing recipes from raw JSON via {@link MockRecipe}.
 * Used when the mod adding the recipe is not present at datagen time, for optional compatability.
 */
public abstract class MockRecipeProvider implements DataProvider {
  private final CompletableFuture<HolderLookup.Provider> registries;
  private final PathProvider recipePathProvider;
  private final PathProvider advancementPathProvider;

  public MockRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
    this.recipePathProvider = output.createRegistryElementsPathProvider(Registries.RECIPE);
    this.advancementPathProvider = output.createRegistryElementsPathProvider(Registries.ADVANCEMENT);
    this.registries = registries;
  }

  /** Adds all mock recipes */
  protected abstract void buildRecipes(MockRecipeConsumer consumer, HolderLookup.Provider registries);

  @Override
  public final CompletableFuture<?> run(CachedOutput output) {
    return this.registries.thenCompose(provider -> this.run(output, provider));
  }

  /** Contains an advancement to serialize. */
  private record ConditionalAdvancement(ResourceLocation id, WithConditions<Advancement> advancement) {
    public ConditionalAdvancement(AdvancementHolder advancement, ICondition... conditions) {
      this(advancement.id(), new WithConditions<>(advancement.value(), conditions));
    }

    /** Serializes this to JSON */
    public JsonElement serialize(MockRecipe.Serializer serializer) {
      return serializer.serialize(Advancement.CONDITIONAL_CODEC, Optional.of(advancement));
    }
  }

  /** Runs with the registry provider */
  protected CompletableFuture<?> run(CachedOutput cachedOutput, HolderLookup.Provider registries) {
    List<MockRecipe> recipes = new ArrayList<>();
    List<ConditionalAdvancement> advancements = new ArrayList<>();
    MockRecipeConsumer consumer = new MockRecipeConsumer() {
      @Override
      public void accept(MockRecipe mockRecipe) {
        recipes.add(mockRecipe);
      }

      @Override
      public void advancement(AdvancementHolder advancement, ICondition... conditions) {
        advancements.add(new ConditionalAdvancement(advancement, conditions));
      }
    };
    buildRecipes(consumer, registries);
    MockRecipe.Serializer serializer = new MockRecipe.Serializer(registries);
    return GenericDataProvider.allOf(Stream.concat(
      recipes.stream().map(recipe -> DataProvider.saveStable(cachedOutput, recipe.serializeRecipe(serializer), recipePathProvider.json(recipe.id()))),
      advancements.stream().map(advancement -> DataProvider.saveStable(cachedOutput, advancement.serialize(serializer), advancementPathProvider.json(advancement.id)))
    ));
  }
}
