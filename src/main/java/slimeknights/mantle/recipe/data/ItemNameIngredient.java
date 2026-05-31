package slimeknights.mantle.recipe.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.List;

/**
 * Ingredient for a non-NBT sensitive item from another mod, should never be used outside datagen
 */
public class ItemNameIngredient {
  private final List<ResourceLocation> names;
  protected ItemNameIngredient(List<ResourceLocation> names) {
    this.names = names;
  }

  /** Creates a new ingredient from a list of names */
  public static ItemNameIngredient from(List<ResourceLocation> names) {
    return new ItemNameIngredient(names);
  }

  /** Creates a new ingredient from a list of names */
  public static ItemNameIngredient from(ResourceLocation... names) {
    return from(Arrays.asList(names));
  }

  /** Creates a JSON object for a name */
  private static JsonObject forName(ResourceLocation name) {
    JsonObject json = new JsonObject();
    json.addProperty("item", name.toString());
    return json;
  }

  public JsonElement toJson() {
    if (names.size() == 1) {
      return forName(names.get(0));
    }
    JsonArray array = new JsonArray();
    for (ResourceLocation name : names) {
      array.add(forName(name));
    }
    return array;
  }

}
