package slimeknights.mantle.recipe.crafting;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.recipe.MantleRecipeSerializers;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@RequiredArgsConstructor(staticName = "fromShaped")
public class ShapedRetexturedRecipeBuilder {
  private final ShapedRecipeBuilder parent;
  private Ingredient texture;
  private boolean matchAll;

  /**
   * Sets the texture source to the given ingredient
   * @param texture Ingredient to use for texture
   * @return Builder instance
   */
  public ShapedRetexturedRecipeBuilder setSource(Ingredient texture) {
    this.texture = texture;
    return this;
  }

  /**
   * Sets the texture source to the given tag
   * @param tag Tag to use for texture
   * @return Builder instance
   */
  public ShapedRetexturedRecipeBuilder setSource(TagKey<Item> tag) {
    this.texture = Ingredient.of(tag);
    return this;
  }

  /**
   * Sets the match first property on the recipe.
   * If set, the recipe uses the first ingredient match for the texture. If unset, all items that match the ingredient must be the same or no texture is applied
   * @return Builder instance
   */
  public ShapedRetexturedRecipeBuilder setMatchAll() {
    this.matchAll = true;
    return this;
  }

  /**
   * Builds the recipe with the default name using the given consumer
   * @param consumer Recipe consumer
   */
  public void build(Consumer<FinishedRecipe> consumer) {
    this.validate();
    parent.save(base -> consumer.accept(new Result(base, texture, matchAll)));
  }

  /**
   * Builds the recipe using the given consumer
   * @param consumer Recipe consumer
   * @param location Recipe location
   */
  public void build(Consumer<FinishedRecipe> consumer, ResourceLocation location) {
    this.validate();
    parent.save(base -> consumer.accept(new Result(base, texture, matchAll)), location);
  }

  /**
   * Ensures this recipe can be built
   * @throws IllegalStateException If the recipe cannot be built
   */
  private void validate() {
    if (texture == null) {
      throw new IllegalStateException("No texture defined for texture recipe");
    }
  }

  private static class Result implements FinishedRecipe {
    private final FinishedRecipe base;
    private final Ingredient texture;
    private final boolean matchAll;

    private Result(FinishedRecipe base, Ingredient texture, boolean matchAll) {
      this.base = base;
      this.texture = texture;
      this.matchAll = matchAll;
    }

    @Override
    public RecipeSerializer<?> getType() {
      return MantleRecipeSerializers.CRAFTING_SHAPED_RETEXTURED;
    }

    @Override
    public ResourceLocation getId() {
      return base.getId();
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      base.serializeRecipeData(json);
      json.add("texture", texture.toJson());
      json.addProperty("match_all", matchAll);
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
