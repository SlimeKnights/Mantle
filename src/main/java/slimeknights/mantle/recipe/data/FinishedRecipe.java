package slimeknights.mantle.recipe.data;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import java.util.Objects;

/** Finished recipe view used by Mantle datagen helpers. */
public interface FinishedRecipe {
  default JsonObject serializeRecipe() {
    JsonObject json = new JsonObject();
    json.addProperty("type", Objects.requireNonNull(BuiltInRegistries.RECIPE_SERIALIZER.getKey(getType())).toString());
    serializeRecipeData(json);
    return json;
  }

  void serializeRecipeData(JsonObject json);

  ResourceLocation getId();

  RecipeSerializer<?> getType();

  @Nullable
  JsonObject serializeAdvancement();

  @Nullable
  ResourceLocation getAdvancementId();
}
