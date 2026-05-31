package slimeknights.mantle.compat.minecraft.data.recipes;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.Objects;

import javax.annotation.Nullable;

/** Compatibility bridge for Mantle's legacy datagen helpers. */
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
