package slimeknights.mantle.recipe.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.conditions.ICondition;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

/** Bridges vanilla 1.21 recipe output back to Mantle's recipe helper API. */
public record VanillaFinishedRecipe(ResourceLocation getId, Recipe<?> recipe, @Nullable AdvancementHolder advancement) implements FinishedRecipe {
  private static final RegistryOps<JsonElement> REGISTRY_OPS = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY).createSerializationContext(JsonOps.INSTANCE);

  /** Creates a recipe output that forwards recipes to Mantle recipe consumers. */
  public static RecipeOutput output(Consumer<FinishedRecipe> consumer) {
    return new RecipeOutput() {
      @Override
      public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
        consumer.accept(new VanillaFinishedRecipe(id, recipe, advancement));
      }

      @Override
      public Advancement.Builder advancement() {
        return Advancement.Builder.recipeAdvancement();
      }
    };
  }

  @Override
  public void serializeRecipeData(JsonObject json) {
    JsonObject encoded = Recipe.CODEC.encodeStart(REGISTRY_OPS, recipe).getOrThrow(JsonSyntaxException::new).getAsJsonObject();
    encoded.remove("type");
    for (Map.Entry<String,com.google.gson.JsonElement> entry : encoded.entrySet()) {
      json.add(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public RecipeSerializer<?> getType() {
    return recipe.getSerializer();
  }

  @Nullable
  @Override
  public JsonObject serializeAdvancement() {
    if (advancement == null) {
      return null;
    }
    return Advancement.CODEC.encodeStart(REGISTRY_OPS, advancement.value()).getOrThrow(JsonSyntaxException::new).getAsJsonObject();
  }

  @Nullable
  @Override
  public ResourceLocation getAdvancementId() {
    return advancement == null ? null : advancement.id();
  }
}
