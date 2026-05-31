package slimeknights.mantle.recipe.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Common logic to create a recipe builder class
 * @param <T>
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class AbstractRecipeBuilder<T extends AbstractRecipeBuilder<T>> {
  /** Advancement builder for this class */
  protected final Advancement.Builder advancementBuilder = Advancement.Builder.advancement();
  /** Number of custom unlock criteria added to the builder. */
  private int criteriaCount = 0;
  /** Group for this recipe */
  @Nonnull
  protected String group = "";

  /**
   * Adds a criteria to the recipe
   * @param name      Criteria name
   * @param criteria  Criteria instance
   * @return  Builder
   */
  @SuppressWarnings("unchecked")
  public T unlockedBy(String name, Criterion<?> criteria) {
    this.advancementBuilder.addCriterion(name, criteria);
    criteriaCount++;
    return (T)this;
  }

  /**
   * Sets the group for this recipe
   * @param group  Recipe group
   * @return  Builder
   */
  @SuppressWarnings("unchecked")
  public T group(String group) {
    this.group = group;
    return (T)this;
  }

  /**
   * Sets the group for this recipe
   * @param group  Recipe resource location group
   * @return  Builder
   */
  public T group(ResourceLocation group) {
    // if minecraft, no namepsace. Groups are technically not namespaced so this is for consistency with vanilla
    if ("minecraft".equals(group.getNamespace())) {
      return group(group.getPath());
    }
    return group(group.toString());
  }

  /**
   * Builds the recipe with a default recipe ID, typically based on the output
   * @param consumerIn  Recipe consumer
   */
  public abstract void save(Consumer<FinishedRecipe> consumerIn);

  /**
   * Builds the recipe
   * @param consumerIn  Recipe consumer
   * @param id          Recipe ID
   */
  public abstract void save(Consumer<FinishedRecipe> consumerIn, ResourceLocation id);

  /**
   * Base logic for advancement building
   * @param id      Recipe ID
   * @param folder  Group folder for saving recipes. Vanilla typically uses item groups, but for mods might as well base on the recipe
   * @return Advancement ID
   */
  private ResourceLocation buildAdvancementInternal(ResourceLocation id, String folder) {
    this.advancementBuilder
        .parent(ResourceLocation.withDefaultNamespace("recipes/root"))
        .rewards(AdvancementRewards.Builder.recipe(id))
        .requirements(AdvancementRequirements.Strategy.OR);
    this.advancementBuilder.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id));
    return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "recipes/" + folder + "/" + id.getPath());
  }

  /**
   * Builds and validates the advancement, intended to be called in {@link #save(Consumer, ResourceLocation)}
   * @param id      Recipe ID
   * @param folder  Group folder for saving recipes. Vanilla typically uses item groups, but for mods might as well base on the recipe
   * @return Advancement ID
   */
  protected ResourceLocation buildAdvancement(ResourceLocation id, String folder) {
    if (criteriaCount == 0) {
      throw new IllegalStateException("No way of obtaining recipe " + id);
    }
    return buildAdvancementInternal(id, folder);
  }

  /**
   * Builds an optional advancement, intended to be called in {@link #save(Consumer, ResourceLocation)}
   * @param id        Recipe ID
   * @param folder    Group folder for saving recipes. Vanilla typically uses item groups, but for mods might as well base on the recipe
   * @return Advancement ID, or null if the advancement was not defined
   */
  @SuppressWarnings("SameParameterValue")  // API
  @Nullable
  protected ResourceLocation buildOptionalAdvancement(ResourceLocation id, String folder) {
    if (criteriaCount == 0) {
      return null;
    }
    return buildAdvancementInternal(id, folder);
  }

  /** Class to implement basic finished recipe methods */
  @Getter
  @RequiredArgsConstructor
  protected abstract class AbstractFinishedRecipe implements FinishedRecipe {
    private final ResourceLocation id;
    @Nullable
    private final ResourceLocation advancementId;

    @Nullable
    @Override
    public JsonObject serializeAdvancement() {
      if (advancementId == null) {
        return null;
      }
      return Advancement.CODEC.encodeStart(JsonOps.INSTANCE, advancementBuilder.build(advancementId).value()).getOrThrow(JsonSyntaxException::new).getAsJsonObject();
    }
  }

  /** Finished recipe using a loadable */
  protected class LoadableFinishedRecipe<R extends Recipe<?>> extends AbstractFinishedRecipe {
    private final R recipe;
    private final RecordLoadable<R> loadable;

    public LoadableFinishedRecipe(ResourceLocation id, R recipe, RecordLoadable<R> loadable, @Nullable ResourceLocation advancementId) {
      super(id, advancementId);
      this.recipe = recipe;
      this.loadable = loadable;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      json.addProperty("id", getId().toString());
      loadable.serialize(recipe, json);
    }

    @Override
    public RecipeSerializer<?> getType() {
      return recipe.getSerializer();
    }
  }
}
