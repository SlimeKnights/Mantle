package slimeknights.mantle.recipe.helper;

import com.google.gson.JsonObject;
import slimeknights.mantle.recipe.data.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;

/** Finished recipe implementation for {@link SimpleRecipeSerializer}, use like {@code consumer.accept(new SimpleFinishedRecipe(...))} */
public record SimpleFinishedRecipe(ResourceLocation getId, RecipeSerializer<?> getType) implements FinishedRecipe {
  @Override
  public void serializeRecipeData(JsonObject pJson) {}

  @Nullable
  @Override
  public JsonObject serializeAdvancement() {
    return null;
  }

  @Nullable
  @Override
  public ResourceLocation getAdvancementId() {
    return null;
  }
}
