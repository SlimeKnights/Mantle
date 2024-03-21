package slimeknights.mantle.data.loadable.primitive;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.loadable.Loadable;

/**
 * Loadable for an integer
 * @param maxLength   Maximum length of string allowed
 */
public record StringLoadable(int maxLength) implements Loadable<String> {
  /** Loadable for the default max string length */
  public static final StringLoadable DEFAULT = new StringLoadable(Short.MAX_VALUE);

  @Override
  public String convert(JsonElement element, String key) {
    String value = GsonHelper.convertToString(element, key);
    if (value.length() > maxLength) {
      throw new JsonSyntaxException(key + " may not be longer than " + maxLength);
    }
    return value;
  }

  @Override
  public JsonElement serialize(String object) {
    return new JsonPrimitive(object);
  }

  @Override
  public String fromNetwork(FriendlyByteBuf buffer) {
    return buffer.readUtf(maxLength);
  }

  @Override
  public void toNetwork(String object, FriendlyByteBuf buffer) {
    buffer.writeUtf(object, maxLength);
  }
}
