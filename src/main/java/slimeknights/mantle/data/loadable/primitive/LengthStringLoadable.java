package slimeknights.mantle.data.loadable.primitive;

import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.util.typed.TypedMap;

/**
 * Implementation of a loadable for a string. Access through {@link StringLoadable#length(int, int)} or related helpers.
 * @param minLength   Minimum length of string allowed.
 * @param maxLength   Maximum length of string allowed.
 */
record LengthStringLoadable(int minLength, int maxLength) implements StringLoadable<String> {
  /** Validates the string length. */
  private void validateString(String value, String key, ErrorFactory error) {
    if (value.length() > maxLength) {
      throw error.create(key + " may not be longer than " + maxLength);
    }
    if (value.length() < minLength) {
      // 1 means you want non-empty, so make a more clear error message.
      if (minLength == 1) {
        throw error.create(key + " may not be an empty string");
      } else {
        throw error.create(key + " may not be shorter than " + minLength);
      }
    }
  }

  @Override
  public String parseString(String value, String key, TypedMap context) {
    validateString(value, key, ErrorFactory.JSON_SYNTAX_ERROR);
    return value;
  }

  @Override
  public String getString(String string) {
    validateString(string, "String", ErrorFactory.RUNTIME);
    return string;
  }

  @Override
  public String decode(FriendlyByteBuf buffer, TypedMap context) {
    return buffer.readUtf(maxLength);
  }

  @Override
  public void encode(FriendlyByteBuf buffer, String object) {
    buffer.writeUtf(object, maxLength);
  }
}
