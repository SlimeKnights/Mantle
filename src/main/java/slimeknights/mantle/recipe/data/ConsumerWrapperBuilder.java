package slimeknights.mantle.recipe.data;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.data.recipes.RecipeOutput;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a recipe output wrapper, adding conditions to recipes.
 *
 * @deprecated In NeoForge 1.21+, use {@link RecipeOutput#withConditions(ICondition...)} directly.
 *             For example: {@code output.withConditions(condition1, condition2)}
 */
@Deprecated(forRemoval = true)
@SuppressWarnings("unused")  // API
public class ConsumerWrapperBuilder {
  private final List<ICondition> conditions = new ArrayList<>();

  private ConsumerWrapperBuilder() {}

  /**
   * Creates a wrapper builder with the default serializer
   * @return Default serializer builder
   * @deprecated Use {@link RecipeOutput#withConditions(ICondition...)} instead
   */
  @Deprecated(forRemoval = true)
  public static ConsumerWrapperBuilder wrap() {
    return new ConsumerWrapperBuilder();
  }

  /**
   * Adds a conditional to the consumer
   * @param condition Condition to add
   * @return Added condition
   * @deprecated Use {@link RecipeOutput#withConditions(ICondition...)} instead
   */
  @Deprecated(forRemoval = true)
  @CanIgnoreReturnValue
  public ConsumerWrapperBuilder addCondition(ICondition condition) {
    conditions.add(condition);
    return this;
  }

  /**
   * Builds the consumer for the wrapper builder
   * @param output Base output
   * @return Wrapped output
   * @deprecated Use {@link RecipeOutput#withConditions(ICondition...)} instead
   */
  @Deprecated(forRemoval = true)
  public RecipeOutput build(RecipeOutput output) {
    if (conditions.isEmpty()) {
      return output;
    }
    return output.withConditions(conditions.toArray(ICondition[]::new));
  }
}
