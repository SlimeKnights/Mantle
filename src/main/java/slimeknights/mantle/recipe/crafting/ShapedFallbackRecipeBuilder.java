package slimeknights.mantle.recipe.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.recipe.MantleRecipeSerializers;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/** Builder for a shaped recipe with fallbacks */
@SuppressWarnings("unused")
@RequiredArgsConstructor(staticName = "fallback")
public class ShapedFallbackRecipeBuilder {
  private final ShapedRecipeBuilder base;
  private final List<ResourceLocation> alternatives = new ArrayList<>();

  /**
   * Adds a single alternative to this recipe. Any matching alternative causes this recipe to fail
   * @param location  Alternative
   * @return  Builder instance
   */
  public ShapedFallbackRecipeBuilder addAlternative(ResourceLocation location) {
    this.alternatives.add(location);
    return this;
  }

  /**
   * Adds a list of alternatives to this recipe. Any matching alternative causes this recipe to fail
   * @param locations  Alternative list
   * @return  Builder instance
   */
  public ShapedFallbackRecipeBuilder addAlternatives(Collection<ResourceLocation> locations) {
    this.alternatives.addAll(locations);
    return this;
  }

  /**
   * Builds the recipe using the output as the name
   * @param consumer  Recipe consumer
   */
  public void build(Consumer<FinishedRecipe> consumer) {
    base.save(base -> consumer.accept(new Result(base, alternatives)));
  }

  /**
   * Builds the recipe using the given ID
   * @param consumer  Recipe consumer
   * @param id        Recipe ID
   */
  public void build(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    base.save(base -> consumer.accept(new Result(base, alternatives)), id);
  }

  private record Result(FinishedRecipe base, List<ResourceLocation> alternatives) implements FinishedRecipe {
    @Override
    public void serializeRecipeData(JsonObject json) {
      base.serializeRecipeData(json);
      json.add("alternatives", alternatives.stream()
                                           .map(ResourceLocation::toString)
                                           .collect(JsonArray::new, JsonArray::add, JsonArray::addAll));
    }

    @Override
    public RecipeSerializer<?> getType() {
      return MantleRecipeSerializers.CRAFTING_SHAPED_FALLBACK;
    }

    @Override
    public ResourceLocation getId() {
      return base.getId();
    }

    @Nullable
    @Override
    public JsonObject serializeAdvancement() {
      return base.serializeAdvancement();
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementId() {
      return base.getAdvancementId();
    }
  }
}
