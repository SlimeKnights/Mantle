package slimeknights.mantle.recipe.data.mock;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.recipe.data.mock.RecipeModification.SetSerializer;

import java.util.List;

/**
 * Helper for creating {@link RecipeOutput} from {@link MockRecipeConsumer}.
 * Note all recipes using this should have at least one {@link RecipeModification}.
 * Otherwise, there is no reason to use this over {@link net.minecraft.world.item.crafting.RecipeSerializer}.
 */
public record MockRecipeOutput(MockRecipeConsumer consumer) {
  /** Creates a wrapper from the given consumer. */
  public static MockRecipeOutput of(MockRecipeConsumer output) {
    return new MockRecipeOutput(output);
  }

  /**
   * Creates a recipe output with the given modifications.
   * Use {@link RecipeOutput#withConditions(ICondition...)} if you wish to additionally add recipe conditions.
   */
  public RecipeOutput output(RecipeModification... modifications) {
    return new MockOutput(consumer, List.of(modifications));
  }

  /**
   * Creates an output that just overrides the serializer.
   * @see SetSerializer
   */
  public RecipeOutput setSerializer(ResourceLocation type) {
    return output(new SetSerializer(type));
  }

  /** Creates a mock recipe output under the given settings. */
  private record MockOutput(MockRecipeConsumer output, List<RecipeModification> modifications) implements RecipeOutput {
    @SuppressWarnings("removal")
    @Override
    public Builder advancement() {
      return Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
    }

    @Override
    public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
      output.accept(new MockRecipeWrapper(id, recipe, modifications), conditions);
      if (advancement != null) {
        output.advancement(advancement, conditions);
      }
    }
  }

  /** Serializes a {@link Recipe} with the given list of JSON modifications. */
  public record MockRecipeWrapper(ResourceLocation id, Recipe<?> recipe, List<RecipeModification> modifications) implements MockRecipe {
    @Override
    public JsonObject serializeRecipe(MockRecipe.Serializer serializer) {
      JsonObject json = serializer.serialize(Recipe.CODEC, recipe).getAsJsonObject();
      for (RecipeModification modification : modifications) {
        modification.apply(json, serializer);
      }
      return json;
    }
  }
}
