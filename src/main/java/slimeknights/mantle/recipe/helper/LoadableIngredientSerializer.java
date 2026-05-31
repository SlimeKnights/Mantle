package slimeknights.mantle.recipe.helper;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.compat.neoforged.neoforge.common.crafting.IIngredientSerializer;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

/** Ingredient serializer made using loadables */
public record LoadableIngredientSerializer<T>(RecordLoadable<T> loadable) implements IIngredientSerializer<T> {
  @Override
  public T parse(FriendlyByteBuf buffer) {
    return loadable.decode(buffer);
  }

  @Override
  public T parse(JsonObject json) {
    return loadable.deserialize(json);
  }

  @Override
  public void write(FriendlyByteBuf buffer, T ingredient) {
    loadable.encode(buffer, ingredient);
  }

  /** Serializes the ingredient to JSON */
  public JsonObject serialize(T ingredient) {
    JsonObject json = new JsonObject();
    json.addProperty("type", "mantle:legacy");
    loadable.serialize(ingredient, json);
    return json;
  }
}
