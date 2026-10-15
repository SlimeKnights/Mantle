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
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.util.LogicHelper;

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

  /** Wraps this consumer with the given conditions list, which will be added to conditions from arguments. Useful for usage inside recipe builders. */
  default MockRecipeConsumer withConditions(ICondition... conditions) {
    return conditions.length == 0 ? this : new Conditional(this, conditions);
  }


  /**
   * Adds extra conditions to the given recipe.
   * @apiNote construct with {@link #accept(MockRecipe, ICondition...)}
   */
  @Internal
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

  /**
   * Wraps a recipe consumer with the given conditions list.
   * @apiNote construct with {@link #withConditions(ICondition...)}.
   */
  @Internal
  record Conditional(MockRecipeConsumer inner, ICondition... conditions) implements MockRecipeConsumer {
    @Override
    public void accept(MockRecipe recipe) {
      inner.accept(recipe, conditions);
    }

    @Override
    public void accept(MockRecipe recipe, ICondition... conditions) {
      if (conditions.length == 0) {
        inner.accept(recipe, this.conditions);
      } else {
        inner.accept(recipe, LogicHelper.append(this.conditions, conditions));
      }
    }

    @Override
    public void advancement(AdvancementHolder advancement, ICondition... conditions) {
      if (conditions.length == 0) {
        inner.advancement(advancement, this.conditions);
      } else {
        inner.advancement(advancement, LogicHelper.append(this.conditions, conditions));
      }
    }
  }
}
