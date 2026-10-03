package slimeknights.mantle.data.loadable.primitive;

import com.google.gson.JsonSyntaxException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.util.typed.TypedMap;

/**
 * Implementation of a loadable for a string. Access through {@link StringLoadable#maxLength(int)}.
 * @param maxLength   Maximum length of string allowed
 */
record MaxLengthStringLoadable(int maxLength) implements StringLoadable<String> {
  @Override
  public String parseString(String value, String key, TypedMap context) {
    if (value.length() > maxLength) {
      throw new JsonSyntaxException(key + " may not be longer than " + maxLength);
    }
    return value;
  }

  @Override
  public String getString(String object) {
    if (object.length() > maxLength) {
      throw new RuntimeException("String may not be longer than " + maxLength);
    }
    return object;
  }

  @Override
  public String decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return buffer.readUtf(maxLength);
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, String object) {
    buffer.writeUtf(object, maxLength);
  }
}
