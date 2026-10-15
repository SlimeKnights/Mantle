package slimeknights.mantle.recipe.data.mock;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.Map;
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

  /** Adds the given advancement to the consumer */
  void advancement(AdvancementHolder advancement, ICondition... conditions);

  /**
   * Adds an advancement from the given criteria map
   * @see net.minecraft.data.recipes.RecipeProvider#has(ItemLike)
   * @see net.minecraft.data.recipes.RecipeProvider#has(TagKey)
   */
  @SuppressWarnings("removal")
  default void advancement(ResourceLocation id, String folder, Map<String, Criterion<?>> criteria, ICondition... conditions) {
    Advancement.Builder builder = Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)
      .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
      .rewards(AdvancementRewards.Builder.recipe(id))
      .requirements(AdvancementRequirements.Strategy.OR);
    criteria.forEach(builder::addCriterion);
    advancement(builder.build(id.withPrefix("recipes/" + folder + '/')), conditions);
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
