package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.data.loadable.Loadable;

/** Loadable for ingredients, handling Forge ingredients */
public enum IngredientLoadable implements Loadable<Ingredient> {
  ALLOW_EMPTY,
  DISALLOW_EMPTY;

  @Override
  public Ingredient convert(JsonElement element, String key) {
    return Ingredient.fromJson(element, this == ALLOW_EMPTY);
  }

  @Override
  public JsonElement serialize(Ingredient object) {
    if (object.isEmpty() && this == DISALLOW_EMPTY) {
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
