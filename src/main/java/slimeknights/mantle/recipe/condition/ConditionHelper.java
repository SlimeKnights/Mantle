package slimeknights.mantle.recipe.condition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.Arrays;
import java.util.List;

/** Compatibility helpers for NeoForge condition codecs. */
public class ConditionHelper {
  private ConditionHelper() {}

  public static JsonElement serialize(ICondition condition) {
    return ICondition.CODEC.encodeStart(JsonOps.INSTANCE, condition).getOrThrow(JsonParseException::new);
  }

  public static JsonArray serialize(ICondition[] conditions) {
    return serialize(Arrays.asList(conditions));
  }

  public static JsonArray serialize(List<ICondition> conditions) {
    JsonArray array = new JsonArray();
    for (ICondition condition : conditions) {
      array.add(serialize(condition));
    }
    return array;
  }

  public static ICondition deserialize(JsonObject json) {
    return ICondition.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(JsonParseException::new);
  }

  public static boolean processConditions(JsonObject json, String key, ICondition.IContext context) {
    if (!json.has(key)) {
      return true;
    }
    JsonElement element = json.get(key);
    if (!element.isJsonArray()) {
      throw new JsonParseException("Expected " + key + " to be an array");
    }
    return processConditions(element.getAsJsonArray(), context);
  }

  public static boolean processConditions(JsonArray conditions, ICondition.IContext context) {
    for (JsonElement element : conditions) {
      ICondition condition = ICondition.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow(JsonParseException::new);
      if (!condition.test(context)) {
        return false;
      }
    }
    return true;
  }
}
