package slimeknights.mantle.recipe.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builds a recipe consumer wrapper, which adds some extra properties to wrap the result of another recipe
 */
public class ConsumerWrapperBuilder {
  private final List<ICondition> conditions = new ArrayList<>();
  @Nullable
  private final IRecipeSerializer<?> override;

  private ConsumerWrapperBuilder(@Nullable IRecipeSerializer<?> override) {
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
  public static ConsumerWrapperBuilder wrap(IRecipeSerializer<?> override) {
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
  public Consumer<IFinishedRecipe> build(Consumer<IFinishedRecipe> consumer) {
    return (recipe) -> consumer.accept(new Wrapped(recipe, conditions, override));
  }

  private static class Wrapped implements IFinishedRecipe {
    private final IFinishedRecipe original;
    private final List<ICondition> conditions;
    @Nullable
    private final IRecipeSerializer<?> override;

    private Wrapped(IFinishedRecipe original, List<ICondition> conditions, @Nullable IRecipeSerializer<?> override) {
      this.original = original;
      this.conditions = conditions;
      this.override = override;
    }

    @Override
    public void serialize(JsonObject json) {
      // add conditions on top
      JsonArray conditionsArray = new JsonArray();
      for (ICondition condition : conditions) {
        conditionsArray.add(CraftingHelper.serialize(condition));
      }
      json.add("conditions", conditionsArray);
      // serialize the normal recipe
      original.serialize(json);
    }

    @Override
    public ResourceLocation getID() {
      return original.getID();
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
      if (override != null) {
        return override;
      }
      return original.getSerializer();
    }

    @Nullable
    @Override
    public JsonObject getAdvancementJson() {
      return original.getAdvancementJson();
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementID() {
      return original.getAdvancementID();
    }
  }
}
