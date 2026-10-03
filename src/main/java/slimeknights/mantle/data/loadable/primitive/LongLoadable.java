package slimeknights.mantle.data.loadable.primitive;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.array.ArrayLoadable;
import slimeknights.mantle.data.loadable.array.LongArrayLoadable;
import slimeknights.mantle.util.typed.TypedMap;

/**
 * Loadable for a long
 * @see IntLoadable
 */
@SuppressWarnings("unused")  // API
@RequiredArgsConstructor
public class LongLoadable implements Loadable<Long> {
  /** Loadable ranging from long min to long max */
  public static final LongLoadable ANY = range(Long.MIN_VALUE, Long.MAX_VALUE);
  /** Loadable ranging from zero to long max */
  public static final LongLoadable FROM_ZERO = min(0);
  /** Loadable ranging from one to long max */
  public static final LongLoadable FROM_ONE = min(1);

  /** Minimum allowed value */
  private final long min;
  /** Maximum allowed value */
  private final long max;
  /** If true, writes using var long. If false, writes using full 8 bytes every time */
  private final boolean var;

  /** Creates a loadable */
  public static LongLoadable range(long min, long max) {
    return new LongLoadable(min, max, true);
  }

  /** Creates a loadable ranging from the parameter to long max */
  public static LongLoadable min(long min) {
    return range(min, Long.MAX_VALUE);
  }

  /** ensures the int is within valid ranges */
  protected long validate(long value, String key) {
    if (min <= value && value <= max) {
      return value;
    }
    if (min == Long.MIN_VALUE) {
      throw new JsonSyntaxException(key + " must not be greater than " + max);
    }
    if (max == Long.MAX_VALUE) {
      throw new JsonSyntaxException(key + " must not be less than " + min);
    }
    throw new JsonSyntaxException(key + " must be between " + min + " and " + max);
  }

  @Override
  public Long convert(JsonElement element, String key, TypedMap context) {
    return validate(GsonHelper.convertToLong(element, key), key);
  }

  @Override
  public JsonElement serialize(Long value) {
    return new JsonPrimitive(validate(value, "Value"));
  }


  /* Networking */

  @Override
  public Long decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return var ? buffer.readVarLong() : buffer.readLong();
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, Long value) {
    if (var) {
      buffer.writeVarLong(value);
    } else {
      buffer.writeLong(value);
    }
  }


  /* Arrays */

  /** Creates a loadable for a integer array */
  public ArrayLoadable<long[]> array(int minSize, int maxSize) {
    return new LongArrayLoadable(this, minSize, maxSize);
  }

  /** Creates a loadable for a integer array */
  public ArrayLoadable<long[]> array(int minSize) {
    return array(minSize, Integer.MAX_VALUE);
  }


  /* Strings */

  /**
   * Creates an int loadable that writes to JSON as a string, can be used as a map key.
   * @param radix  Base for conversion, base 10 is standard JSON numbers.
   */
  public StringLoadable<Long> asString(int radix) {
    return new StringLongLoadable(min, max, radix, var);
  }


  /** Writes to a string instead of to an integer */
  private static class StringLongLoadable extends LongLoadable implements StringLoadable<Long> {
    private final int radix;
    public StringLongLoadable(long min, long max, int radix, boolean var) {
      super(min, max, var);
      if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
        throw new IllegalArgumentException("Invalid radix " + radix + ", must be between " + Character.MIN_RADIX + " and " + Character.MAX_RADIX);
      }
      this.radix = radix;
    }

    @Override
    public Long parseString(String value, String key, TypedMap context) {
      try {
        return validate(Long.parseLong(value, radix), key);
      } catch (NumberFormatException e) {
        throw new JsonSyntaxException("Failed to parse long at " + key, e);
      }
    }

    @Override
    public Long convert(JsonElement element, String key, TypedMap context) {
      return parseString(GsonHelper.convertToString(element, key), key, context);
    }

    @Override
    public String getString(Long value) {
      return Long.toString(value, radix);
    }

    @Override
    public JsonElement serialize(Long value) {
      return new JsonPrimitive(getString(value));
    }
  }
}
