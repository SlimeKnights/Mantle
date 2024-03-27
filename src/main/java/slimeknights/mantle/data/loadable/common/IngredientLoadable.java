package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.data.loadable.Loadable;

/** Loadable for ingredients, handling Forge ingredients */
public enum IngredientLoadable implements Loadable<Ingredient> {
  ALLOW_EMPTY,
  DISALLOW_EMPTY;

  @Override
  public Ingredient convert(JsonElement element, String key) {
    Ingredient ingredient = Ingredient.fromJson(element);
    if (ingredient == Ingredient.EMPTY && this == DISALLOW_EMPTY) {
      throw new JsonSyntaxException("Ingredient cannot be empty");
    }
    return ingredient;
  }

  @Override
  public JsonElement serialize(Ingredient object) {
    if (object == Ingredient.EMPTY) {
      if (this == ALLOW_EMPTY) {
        return JsonNull.INSTANCE;
      }
      throw new IllegalArgumentException("Ingredient cannot be empty");
    }
    return object.toJson();
  }

  @Override
  public Ingredient decode(FriendlyByteBuf buffer) {
    return Ingredient.fromNetwork(buffer);
  }

  @Override
  public void encode(FriendlyByteBuf buffer, Ingredient object) {
    object.toNetwork(buffer);
  }
}
