package slimeknights.mantle.recipe.data.mock;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.function.Consumer;

/**
 * Mirror of {@link net.minecraft.data.recipes.RecipeOutput} for {@link MockRecipe}.
 * Used for serializing recipes from other mods without a datagen dependency.
 * @see MockRecipeProvider
 */
public interface MockRecipeConsumer extends Consumer<MockRecipe> {
  /** Adds the given recipe with conditions. */
  default void accept(MockRecipe recipe, ICondition... conditions) {
    if (conditions.length == 0) {
      accept(recipe);
    } else {
      accept(new WithConditions(recipe, conditions));
    }
  }

  /** Adds extra conditions to the given recipe. Generally no need to construct this class, use {@link #accept(MockRecipe, ICondition...)} */
  record WithConditions(MockRecipe recipe, ICondition... conditions) implements MockRecipe {
    @Override
    public ResourceLocation id() {
      return recipe.id();
    }

    @Override
    public JsonObject serializeRecipe(MockRecipe.Serializer serializer) {
      JsonObject json = recipe.serializeRecipe(serializer);
      serializer.addConditions(json, conditions);
      return json;
    }
  }
}
