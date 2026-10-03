package slimeknights.mantle.client.book.data.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Locale;

/** Deserializes an integer or string. String may use a {@code 0b} prefix for binary or {@code 0x} prefix for hex. */
public class HexStringDeserializer implements JsonDeserializer<Integer> {
  @Nullable
  @Override
  public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    if (json.isJsonPrimitive()) {
      JsonPrimitive primitive = json.getAsJsonPrimitive();
      if (primitive.isNumber()) {
        return json.getAsInt();
      } else if (primitive.isString()) {
        try {
          String str = json.getAsString().toLowerCase(Locale.ROOT);
          if (str.startsWith("0b")) {
            return Integer.parseInt(str.substring(2), 2);
          } else if (str.startsWith("0x")) {
            return Integer.parseInt(str.substring(2), 16);
          }
        } catch (NumberFormatException ignored) {
        }
      }
    }
    return null;
  }
}
