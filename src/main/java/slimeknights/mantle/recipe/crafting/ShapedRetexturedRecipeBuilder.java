package slimeknights.mantle.recipe.crafting;

import com.google.gson.JsonObject;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonFactory;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class ShapedRetexturedRecipeBuilder {
  private final ShapedRecipeJsonFactory parent;
  private Ingredient texture;
  private boolean matchAll;

  public ShapedRetexturedRecipeBuilder(ShapedRecipeJsonFactory parent) {
    this.parent = parent;
  }

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
  public ShapedRetexturedRecipeBuilder setSource(Tag<Item> tag) {
    this.texture = Ingredient.fromTag(tag);
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
  public void build(Consumer<RecipeJsonProvider> consumer) {
    this.validate();
    parent.offerTo(base -> consumer.accept(new Result(base, texture, matchAll)));
  }

  /**
   * Builds the recipe using the given consumer
   * @param consumer Recipe consumer
   * @param location Recipe location
   */
  public void build(Consumer<RecipeJsonProvider> consumer, Identifier location) {
    this.validate();
    parent.offerTo(base -> consumer.accept(new Result(base, texture, matchAll)), location);
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

  private static class Result implements RecipeJsonProvider {
    private final RecipeJsonProvider base;
    private final Ingredient texture;
    private final boolean matchAll;

    private Result(RecipeJsonProvider base, Ingredient texture, boolean matchAll) {
      this.base = base;
      this.texture = texture;
      this.matchAll = matchAll;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.SHAPED;
    }

    @Override
    public Identifier getRecipeId() {
      return base.getRecipeId();
    }

    @Override
    public void serialize(JsonObject json) {
      base.serialize(json);
      json.add("texture", texture.toJson());
      json.addProperty("match_all", matchAll);
    }

    @Nullable
    @Override
    public JsonObject toAdvancementJson() {
      return base.toAdvancementJson();
    }

    @Nullable
    @Override
    public Identifier getAdvancementId() {
      return base.getAdvancementId();
    }
  }
}
