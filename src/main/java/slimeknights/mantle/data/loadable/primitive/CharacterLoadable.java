package slimeknights.mantle.data.loadable.primitive;

import com.google.gson.JsonSyntaxException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.data.loadable.array.ArrayLoadable;
import slimeknights.mantle.data.loadable.array.CharArrayLoadable;
import slimeknights.mantle.util.typed.TypedMap;

/** Loadable for a single character */
@SuppressWarnings("unused")  // API
public enum CharacterLoadable implements StringLoadable<Character> {
  INSTANCE;
  /** Helper to hide static constant in string loadable */
  public static final CharacterLoadable DEFAULT = INSTANCE;

  @Override
  public Character parseString(String value, String key, TypedMap context) {
    if (value.length() == 1) {
      return value.charAt(0);
    }
    throw new JsonSyntaxException("Too many characters at '" + key + "'");
  }

  @Override
  public String getString(Character object) {
    return object.toString();
  }

  @Override
  public Character decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return buffer.readChar();
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, Character value) {
    buffer.writeChar(value);
  }

  /** Creates a loadable for a character array */
  public ArrayLoadable<char[]> array(int minSize, int maxSize) {
    return new CharArrayLoadable(this, minSize, maxSize);
  }

  /** Creates a loadable for a character array */
  public ArrayLoadable<char[]> array(int minSize) {
    return array(minSize, Integer.MAX_VALUE);
  }
}
