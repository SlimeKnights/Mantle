package slimeknights.mantle.recipe.helper;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import javax.annotation.Nullable;

/**
 * Recipe serializer that logs network exceptions before throwing them as otherwise the exceptions may be invisible
 * @param <T>  Recipe class
 */
public interface LoggingRecipeSerializer<T extends Recipe<?>> extends RecipeSerializer<T> {
  ResourceLocation UNKNOWN_ID = ResourceLocation.fromNamespaceAndPath("mantle", "unknown");
  LegacySerializer<ShapedRecipe> SHAPED_RECIPE = new LegacySerializer<>(RecipeSerializer.SHAPED_RECIPE);
  LegacySerializer<ShapelessRecipe> SHAPELESS_RECIPE = new LegacySerializer<>(RecipeSerializer.SHAPELESS_RECIPE);

  T fromJson(ResourceLocation recipeId, JsonObject json);

  @Nullable
  default T fromNetworkSafe(ResourceLocation recipeId, FriendlyByteBuf buffer) {
    return streamCodec().decode((RegistryFriendlyByteBuf)buffer);
  }

  default void toNetworkSafe(FriendlyByteBuf buffer, T recipe) {
    streamCodec().encode((RegistryFriendlyByteBuf)buffer, recipe);
  }

  @Override
  default MapCodec<T> codec() {
    return MapCodec.assumeMapUnsafe(Codec.PASSTHROUGH.xmap(dynamic -> {
      JsonObject json = dynamic.convert(JsonOps.INSTANCE).getValue().getAsJsonObject();
      return fromJson(UNKNOWN_ID, json);
    }, recipe -> new Dynamic<>(JsonOps.INSTANCE, new JsonObject())));
  }

  @Override
  default StreamCodec<RegistryFriendlyByteBuf,T> streamCodec() {
    return StreamCodec.of((buffer, recipe) -> toNetworkSafe(buffer, recipe), buffer -> fromNetworkSafe(UNKNOWN_ID, buffer));
  }

  record LegacySerializer<R extends Recipe<?>>(RecipeSerializer<R> serializer) {
    public R fromJson(ResourceLocation recipeId, JsonObject json) {
      return serializer.codec().codec().parse(JsonOps.INSTANCE, upgradeLegacyItemStacks(json)).getOrThrow(IllegalArgumentException::new);
    }

    @Nullable
    public R fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
      return serializer.streamCodec().decode((RegistryFriendlyByteBuf)buffer);
    }

    public void toNetwork(FriendlyByteBuf buffer, R recipe) {
      serializer.streamCodec().encode((RegistryFriendlyByteBuf)buffer, recipe);
    }

    /** Minecraft 1.21 item stack JSON uses "id" and components instead of the old "item" and "nbt" keys. */
    private static JsonObject upgradeLegacyItemStacks(JsonObject json) {
      JsonObject copy = json.deepCopy();
      if (copy.has("result") && copy.get("result").isJsonObject()) {
        upgradeLegacyItemStack(copy.getAsJsonObject("result"));
      }
      return copy;
    }

    private static void upgradeLegacyItemStack(JsonObject stack) {
      if (stack.has("item") && !stack.has("id")) {
        stack.add("id", stack.remove("item"));
      }
      if (stack.has("nbt") && stack.get("nbt").isJsonObject()) {
        JsonObject nbt = stack.getAsJsonObject("nbt");
        if (nbt.has("display") && nbt.get("display").isJsonObject()) {
          JsonObject display = nbt.getAsJsonObject("display");
          if (display.has("Name")) {
            JsonObject components = stack.has("components") && stack.get("components").isJsonObject() ? stack.getAsJsonObject("components") : new JsonObject();
            components.add("minecraft:custom_name", display.get("Name"));
            stack.add("components", components);
          }
        }
        stack.remove("nbt");
      }
    }

  }
}
