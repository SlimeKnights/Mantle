package slimeknights.mantle.util;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.util.JSONUtils;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Utilities to help in parsing JSON
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonHelper {
  /**
   * Gets an element from JSON, throwing an exception if missing
   * @param json        Object parent
   * @param memberName  Name to get
   * @return  JsonElement found
   * @throws JsonSyntaxException if element is missing
   */
  public static JsonElement getElement(JsonObject json, String memberName) {
    if (json.has(memberName)) {
      return json.get(memberName);
    } else {
      throw new JsonSyntaxException("Missing " + memberName + "");
    }
  }

  /**
   * Parses a list from an JsonArray
   * @param array   Json array
   * @param name    Json key of the array
   * @param parser  Function to get a raw type from the JsonElement, typically from {@link JSONUtils}
   * @param mapper  Mapper from raw type to new object
   * @param <R>     Element raw type
   * @param <T>     Output type
   * @return  List of output objects
   */
  public static <R,T> List<T> parseList(JsonArray array, String name, BiFunction<JsonElement,String,R> parser, Function<R, T> mapper) {
    if (array.size() == 0) {
      throw new JsonSyntaxException(name + " must have at least 1 element");
    }
    // build the list
    ImmutableList.Builder<T> builder = ImmutableList.builder();
    for (int i = 0; i < array.size(); i++) {
      builder.add(mapper.apply(parser.apply(array.get(i), name + "[" + i + "]")));
    }
    return builder.build();
  }

  /**
   * Parses a list from an JsonArray
   * @param object  Parent JSON object
   * @param name    Json key of the array
   * @param parser  Function to get a raw type from the JsonElement, typically from {@link JSONUtils}
   * @param mapper  Mapper from raw type to new object
   * @param <R>     Element raw type
   * @param <T>     Output type
   * @return  List of output objects
   */
  public static <R,T> List<T> parseList(JsonObject object, String name, BiFunction<JsonElement,String,R> parser, Function<R, T> mapper) {
    return parseList(JSONUtils.getJsonArray(object, name), name, parser, mapper);
  }
}
