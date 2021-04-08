package slimeknights.mantle.recipe.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.recipe.ICondition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builds a recipe consumer wrapper, which adds some extra properties to wrap the result of another recipe
 */
public class ConsumerWrapperBuilder {
  private final List<ICondition> conditions = new ArrayList<>();
  @Nullable
  private final RecipeSerializer<?> override;

  private ConsumerWrapperBuilder(@Nullable RecipeSerializer<?> override) {
    this.override = override;
  }

  /**
   * Creates a wrapper builder with the default serializer
   * @return Default serializer builder
   */
  public static ConsumerWrapperBuilder wrap() {
    return new ConsumerWrapperBuilder(null);
  }

  /**
   * Creates a wrapper builder with a serializer override
   * @param override Serializer override
   * @return Default serializer builder
   */
  public static ConsumerWrapperBuilder wrap(RecipeSerializer<?> override) {
    return new ConsumerWrapperBuilder(override);
  }

  /**
   * Adds a conditional to the consumer
   * @param condition Condition to add
   * @return Added condition
   */
  public ConsumerWrapperBuilder addCondition(ICondition condition) {
    conditions.add(condition);
    return this;
  }

  /**
   * Builds the consumer for the wrapper builder
   * @param consumer Base consumer
   * @return Built wrapper consumer
   */
  public Consumer<RecipeJsonProvider> build(Consumer<RecipeJsonProvider> consumer) {
    return (recipe) -> consumer.accept(new Wrapped(recipe, conditions, override));
  }

  private static class Wrapped implements RecipeJsonProvider {
    private final RecipeJsonProvider original;
    private final List<ICondition> conditions;
    @Nullable
    private final RecipeSerializer<?> override;

    private Wrapped(RecipeJsonProvider original, List<ICondition> conditions, @Nullable RecipeSerializer<?> override) {
      this.original = original;
      this.conditions = conditions;
      this.override = override;
    }

    @Override
    public void serialize(JsonObject json) {
      throw new RuntimeException("Crafting Helper needs help!");
//       add conditions on top
//      JsonArray conditionsArray = new JsonArray();
//      for (ICondition condition : conditions) {
//        conditionsArray.add(CraftingHelper.serialize(condition));
//      }
//      json.add("conditions", conditionsArray);
//       serialize the normal recipe
//      original.serialize(json);
    }

    @Override
    public Identifier getRecipeId() {
      return original.getRecipeId();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
      if (override != null) {
        return override;
      }
      return original.getSerializer();
    }

    @Nullable
    @Override
    public JsonObject toAdvancementJson() {
      return original.toAdvancementJson();
    }

    @Nullable
    @Override
    public Identifier getAdvancementId() {
      return original.getAdvancementId();
    }
  }
}
