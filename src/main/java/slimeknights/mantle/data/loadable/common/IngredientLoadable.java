package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.recipe.data.ItemNameIngredient;
import slimeknights.mantle.util.typed.TypedMap;

/** Loadable for ingredients, handling Forge ingredients */
public enum IngredientLoadable implements Loadable<Ingredient> {
  ALLOW_EMPTY,
  DISALLOW_EMPTY;

  @Override
  public Ingredient convert(JsonElement element, String key, TypedMap context) {
    element = normalizeNestedIngredients(element, true);
    if (element.isJsonObject()) {
      JsonObject object = element.getAsJsonObject();
      if (object.has("type") && "forge:nbt".equals(object.get("type").getAsString())) {
        ItemStack stack = ItemStackLoadable.REQUIRED_STACK_NBT.deserialize(object, context);
        return Ingredient.of(stack);
      }
    }
    return (this == ALLOW_EMPTY ? Ingredient.CODEC : Ingredient.CODEC_NONEMPTY).parse(JsonOps.INSTANCE, element).getOrThrow(JsonParseException::new);
  }

  /** NeoForge's ingredient map codec no longer accepts array ingredients nested inside custom ingredient children. */
  private static JsonElement normalizeNestedIngredients(JsonElement element, boolean topLevel) {
    if (element.isJsonArray()) {
      JsonArray array = element.getAsJsonArray();
      JsonArray normalized = new JsonArray();
      for (JsonElement child : array) {
        normalized.add(normalizeNestedIngredients(child, false));
      }
      if (topLevel) {
        return normalized;
      }
      JsonObject compound = new JsonObject();
      compound.addProperty("type", "neoforge:compound");
      compound.add("children", normalized);
      return compound;
    }
    if (element.isJsonObject()) {
      JsonObject normalized = new JsonObject();
      for (var entry : element.getAsJsonObject().entrySet()) {
        JsonElement value = entry.getValue();
        if (value.isJsonArray()) {
          JsonArray array = new JsonArray();
          for (JsonElement child : value.getAsJsonArray()) {
            array.add(normalizeNestedIngredients(child, false));
          }
          normalized.add(entry.getKey(), array);
        } else {
          normalized.add(entry.getKey(), normalizeNestedIngredients(value, false));
        }
      }
      return normalized;
    }
    return element;
  }

  @Override
  public JsonElement serialize(Ingredient object) {
    if (object.isEmpty() && this == DISALLOW_EMPTY) {
      throw new IllegalArgumentException("Ingredient cannot be empty");
    }
    JsonElement namedItem = ItemNameIngredient.serialize(object);
    if (namedItem != null) {
      return namedItem;
    }
    return (this == ALLOW_EMPTY ? Ingredient.CODEC : Ingredient.CODEC_NONEMPTY).encodeStart(JsonOps.INSTANCE, object).getOrThrow(JsonParseException::new);
  }

  @Override
  public Ingredient decode(FriendlyByteBuf buffer, TypedMap context) {
    return Ingredient.CONTENTS_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer);
  }

  @Override
  public void encode(FriendlyByteBuf buffer, Ingredient object) {
    Ingredient.CONTENTS_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, object);
  }
}
