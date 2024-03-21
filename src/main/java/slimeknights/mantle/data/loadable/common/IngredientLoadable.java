package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.data.loadable.Loadable;

/** Loadable for ingredients, handling Forge ingredients */
public enum IngredientLoadable implements Loadable<Ingredient> {
  ALLOW_EMPTY,
  DISALLOW_EMPTY;

  @Override
  public Ingredient getAndDeserialize(JsonObject parent, String key) {
    if (this == ALLOW_EMPTY && !parent.has(key)) {
      return Ingredient.EMPTY;
    }
    return Loadable.super.getAndDeserialize(parent, key);
  }

  @Override
  public Ingredient convert(JsonElement element, String key) throws JsonSyntaxException {
    Ingredient ingredient = Ingredient.fromJson(element);
    if (ingredient == Ingredient.EMPTY && this == DISALLOW_EMPTY) {
      throw new JsonSyntaxException("Ingredient cannot be empty");
    }
    return ingredient;
  }

  @Override
  public JsonElement serialize(Ingredient object) throws RuntimeException {
    if (object == Ingredient.EMPTY) {
      if (this == ALLOW_EMPTY) {
        return JsonNull.INSTANCE;
      }
      throw new IllegalArgumentException("Ingredient cannot be empty");
    }
    return object.toJson();
  }

  @Override
  public Ingredient fromNetwork(FriendlyByteBuf buffer) throws DecoderException {
    return Ingredient.fromNetwork(buffer);
  }

  @Override
  public void toNetwork(Ingredient object, FriendlyByteBuf buffer) throws EncoderException {
    object.toNetwork(buffer);
  }
}
