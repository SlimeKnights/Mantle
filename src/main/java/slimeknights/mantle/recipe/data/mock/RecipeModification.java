package slimeknights.mantle.recipe.data.mock;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.mantle.util.JsonHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper to run modifications on a {@link net.minecraft.world.item.crafting.Recipe} after serialization.
 * Could theoretically be used for other JSON objects, but designed for usage in {@link MockRecipe}.
 * Note that {@link net.minecraft.data.recipes.RecipeOutput#withConditions(ICondition...)} or {@link MockRecipeConsumer#withConditions(ICondition...)} should be used for conditions over this system as it provides merging behavior.
 * @see MockRecipeOutput
 */
public interface RecipeModification {
  /** Applies this modification to the recipe. */
  void apply(JsonObject recipe, MockRecipe.Serializer serializer);


  /**
   * Runs a modification on a specific element at the given path through nested JSON objects and arrays. Will error if any part of the path is invalid.
   * Any keys in the format {@code [#]} where {@code #} is a number will be treated as indexes in a {@link JsonArray}. Otherwise, they will be keys in {@link JsonObject}.
   */
  interface RecipePathModification extends RecipeModification {
    /** Gets the path to the element to modify. Will be the parent for an insert or delete operation. */
    List<String> path();

    /** Applies this modification to the given element */
    void applyAtPath(JsonElement element, MockRecipe.Serializer serializer);

    /** Gets the key for parsing the last element on the path. Used if you need to convert it to a known type. */
    default String lastKey() {
      List<String> path = path();
      return path.isEmpty() ? "" : path.getLast();
    }

    /** Converts the given element to an object using {@link #lastKey()}. */
    default JsonObject asObject(JsonElement element) {
      return GsonHelper.convertToJsonObject(element, lastKey());
    }

    /** Gets the element at the given key */
    private static JsonElement get(String lastKey, JsonElement element, String key) {
      // if the key is something like [1], treat it as an array index
      if (key.startsWith("[") && key.endsWith("]")) {
        int index = Integer.parseInt(key.substring(1, key.length() - 1));
        return GsonHelper.convertToJsonArray(element, lastKey).get(index);
      }
      // otherwise treat it as an object key.
      return GsonHelper.convertToJsonObject(element, lastKey).get(key);
    }

    /** Applies this modification to the recipe. */
    @Override
    default void apply(JsonObject recipe, MockRecipe.Serializer serializer) {
      JsonElement element = recipe;
      String last = "";
      // iterate the path to find the target element
      for (String path : path()) {
        element = JsonHelper.getElement(GsonHelper.convertToJsonObject(element, last), path);
        last = path;
      }
      applyAtPath(element, serializer);
    }
  }


  /* Implementations */

  /** Overrides the serializer ID. Useful for modded recipes that match the format of a vanilla one, such as Ceramics kiln recipes with {@link slimeknights.mantle.recipe.cooking.CookingRecipeBuilder}. */
  record SetSerializer(ResourceLocation type) implements RecipeModification {
    @Override
    public void apply(JsonObject object, MockRecipe.Serializer serializer) {
      object.addProperty("type", type.toString());
    }
  }

  /** Sets the element at the given path to the passed value. Notably useful to replace a placeholder registry ID with a modded value that is not present. */
  record SetElement(List<String> path, String key, JsonElement value) implements RecipePathModification {
    @Override
    public void applyAtPath(JsonElement element, MockRecipe.Serializer serializer) {
      asObject(element).add(key, value);
    }
  }

  /** Adds a new element to the JSON */
  record SetCodec<T>(List<String> path, String key, Codec<T> codec, T value) implements RecipePathModification {
    @Override
    public void applyAtPath(JsonElement element, MockRecipe.Serializer serializer) {
      asObject(element).add(key, serializer.serialize(codec, value));
    }
  }

  /** Adds a list of elements to the JSON */
  record SetCodecList<T>(List<String> path, String key, Codec<T> codec, List<T> value) implements RecipePathModification {
    @Override
    public void applyAtPath(JsonElement element, MockRecipe.Serializer serializer) {
      asObject(element).add(key, serializer.serializeList(codec, value));
    }
  }

  /** Builder for {@link SetElement} and {@link SetCodec} */
  class PathBuilder {
    private final List<String> path = new ArrayList<>();

    private PathBuilder() {}

    /** Adds the given keys to the path. */
    public static PathBuilder of(String... path) {
      return new PathBuilder().path(path);
    }

    /** Adds the given {@link JsonObject} keys to the path. */
    @CanIgnoreReturnValue
    public PathBuilder path(String... path) {
      Collections.addAll(this.path, path);
      return this;
    }

    /** Adds the  given {@link JsonArray} index to the path. */
    @CanIgnoreReturnValue
    public PathBuilder index(int index) {
      this.path.add("[" + index + ']');
      return this;
    }


    /* Terminal */

    /** Gets the path as an immutable list. */
    private List<String> getPath() {
      return List.copyOf(path);
    }

    /** Creates {@link SetElement} for the given element. */
    public SetElement set(String key, JsonElement element) {
      return new SetElement(getPath(), key, element);
    }

    /** Creates {@link SetElement} for the given string. */
    public SetElement set(String key, String value) {
      return set(key, new JsonPrimitive(value));
    }

    /** Creates {@link SetElement} for the given number. */
    public SetElement set(String key, Number value) {
      return set(key, new JsonPrimitive(value));
    }

    /** Creates {@link SetCodec} for the given value. */
    public <T> SetCodec<T> set(String key, Codec<T> codec, T value) {
      return new SetCodec<>(getPath(), key, codec, value);
    }

    /** Creates {@link SetCodecList} for the given list. */
    public <T> SetCodecList<T> setList(String key, Codec<T> codec, List<T> list) {
      return new SetCodecList<>(getPath(), key, codec, list);
    }

    /** Creates {@link SetCodecList} for the given array. */
    public <T> SetCodecList<T> setArray(String key, Codec<T> codec, T... values) {
      return setList(key, codec, List.of(values));
    }
  }
}
