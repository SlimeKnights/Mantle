package slimeknights.mantle.recipe.data;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.util.Identifier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Common logic to create a recipe builder class
 * @param <T>
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class AbstractRecipeBuilder<T extends AbstractRecipeBuilder<T>> {
  /** Advancement builder for this class */
  protected final Advancement.Task advancementBuilder = Advancement.Task.create();
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
  public T addCriterion(String name, CriterionConditions criteria) {
    this.advancementBuilder.criterion(name, criteria);
    return (T)this;
  }

  /**
   * Sets the group for this recipe
   * @param group  Recipe group
   * @return  Builder
   */
  @SuppressWarnings("unchecked")
  public T setGroup(String group) {
    this.group = group;
    return (T)this;
  }

  /**
   * Sets the group for this recipe
   * @param group  Recipe resource location group
   * @return  Builder
   */
  public T setGroup(Identifier group) {
    // if minecraft, no namepsace. Groups are technically not namespaced so this is for consistency with vanilla
    if ("minecraft".equals(group.getNamespace())) {
      return setGroup(group.getPath());
    }
    return setGroup(group.toString());
  }

  /**
   * Builds the recipe with a default recipe ID, typically based on the output
   * @param consumerIn  Recipe consumer
   */
  public abstract void build(Consumer<RecipeJsonProvider> consumerIn);

  /**
   * Builds the recipe
   * @param consumerIn  Recipe consumer
   * @param id          Recipe ID
   */
  public abstract void build(Consumer<RecipeJsonProvider> consumerIn, Identifier id);

  /**
   * Base logic for advancement building
   * @param id      Recipe ID
   * @param folder  Group folder for saving recipes. Vanilla typically uses item groups, but for mods might as well base on the recipe
   * @return Advancement ID
   */
  private Identifier buildAdvancementInternal(Identifier id, String folder) {
    this.advancementBuilder
        .parent(new Identifier("recipes/root"))
        .criterion("has_the_recipe", RecipeUnlockedCriterion.create(id))
        .rewards(AdvancementRewards.Builder.recipe(id))
        .criteriaMerger(CriterionMerger.OR);
    return new Identifier(id.getNamespace(), "recipes/" + folder + "/" + id.getPath());
  }

  /**
   * Builds and validates the advancement, intended to be called in {@link #build(Consumer, Identifier)}
   * @param id      Recipe ID
   * @param folder  Group folder for saving recipes. Vanilla typically uses item groups, but for mods might as well base on the recipe
   * @return Advancement ID
   */
  protected Identifier buildAdvancement(Identifier id, String folder) {
    if (this.advancementBuilder.getCriteria().isEmpty()) {
      throw new IllegalStateException("No way of obtaining recipe " + id);
    }
    return buildAdvancementInternal(id, folder);
  }

  /**
   * Builds an optional advancement, intended to be called in {@link #build(Consumer, Identifier)}
   * @param id        Recipe ID
   * @param folder    Group folder for saving recipes. Vanilla typically uses item groups, but for mods might as well base on the recipe
   * @return Advancement ID, or null if the advancement was not defined
   */
  @Nullable
  protected Identifier buildOptionalAdvancement(Identifier id, String folder) {
    if (this.advancementBuilder.getCriteria().isEmpty()) {
      return null;
    }
    return buildAdvancementInternal(id, folder);
  }

  /** Class to implement basic finished recipe methods */
  @RequiredArgsConstructor
  protected abstract class AbstractFinishedRecipe implements RecipeJsonProvider {
    @Getter
    private final Identifier ID;
    @Getter @Nullable
    private final Identifier advancementID;

    @Nullable
    @Override
    public JsonObject toAdvancementJson() {
      if (advancementID == null) {
        return null;
      }
      return advancementBuilder.toJson();
    }
  }
}
