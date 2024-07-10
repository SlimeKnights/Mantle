package slimeknights.mantle.data.loadable.primitive;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.loadable.Loadable;

/**
 * Loadable for an integer.
 * @see slimeknights.mantle.data.loadable.common.ColorLoadable
 */
@RequiredArgsConstructor
public class IntLoadable implements Loadable<Integer> {
  /** Loadable ranging from integer min to integer max */
  public static final IntLoadable ANY_FULL = range(Integer.MIN_VALUE, Integer.MAX_VALUE);
  /** Loadable ranging from short min to short max */
  public static final IntLoadable ANY_SHORT = range(Short.MIN_VALUE, Short.MAX_VALUE);
  /** Loadable ranging from -1 to integer max */
  public static final IntLoadable FROM_MINUS_ONE = range(-1, Short.MAX_VALUE);
  /** Loadable ranging from zero to integer max */
  public static final IntLoadable FROM_ZERO = min(0);
  /** Loadable ranging from one to integer max */
  public static final IntLoadable FROM_ONE = min(1);

  /** Minimum allowed value */
  private final int min;
  /** Maximum allowed value */
  private final int max;
  /** Method of writing to the network */
  private final IntNetwork network;

  /** Creates a loadable with defaulting networking */
  public static IntLoadable range(int min, int max) {
    return new IntLoadable(min, max, IntNetwork.recommended(min, max));
  }

  /** Creates a loadable ranging from the parameter to short max */
  public static IntLoadable min(int min) {
    return range(min, Integer.MAX_VALUE);
  }

  /** ensures the int is within valid ranges */
  protected int validate(int value, String key) {
    if (min <= value && value <= max) {
      return value;
    }
    if (min == Integer.MIN_VALUE) {
      throw new JsonSyntaxException(key + " must not be greater than " + max);
    }
    if (max == Integer.MAX_VALUE) {
      throw new JsonSyntaxException(key + " must not be less than " + min);
    }
    throw new JsonSyntaxException(key + " must be between " + min + " and " + max);
  }

  @Override
  public Integer convert(JsonElement element, String key) {
    return validate(GsonHelper.convertToInt(element, key), key);
  }

  @Override
  public JsonElement serialize(Integer value) {
    return new JsonPrimitive(validate(value, "Value"));
  }


  /* Networking */

  @Override
  public Integer decode(FriendlyByteBuf buffer) {
    return network.fromNetwork(buffer);
  }

  @Override
  public void encode(FriendlyByteBuf buffer, Integer object) {
    network.toNetwork(object, buffer);
  }

  /** Methods of writing an int to the network */
  public enum IntNetwork {
    INT {
      @Override
      int fromNetwork(FriendlyByteBuf buffer) {
        return buffer.readInt();
      }

      @Override
      void toNetwork(int value, FriendlyByteBuf buffer) {
        buffer.writeInt(value);
      }
    },
    VAR_INT {
      @Override
      int fromNetwork(FriendlyByteBuf buffer) {
        return buffer.readVarInt();
      }

      @Override
      void toNetwork(int value, FriendlyByteBuf buffer) {
        buffer.writeVarInt(value);
      }
    },
    SHORT {
      @Override
      int fromNetwork(FriendlyByteBuf buffer) {
        return buffer.readShort();
      }

      @Override
      void toNetwork(int value, FriendlyByteBuf buffer) {
        buffer.writeShort(value);
      }
    };

    /** Reads the int from the network */
    abstract int fromNetwork(FriendlyByteBuf buffer);

    /** Writes the int to the network */
    abstract void toNetwork(int value, FriendlyByteBuf buffer);

    /** Recommended int network type based on the ranged */
    public static IntNetwork recommended(int min, int max) {
      if (min >= 0) {
        return IntNetwork.VAR_INT;
      }
      if (min >= Short.MIN_VALUE && max <= Short.MAX_VALUE) {
        return IntNetwork.SHORT;
      }
      return IntNetwork.INT;
    }
  }


  /* Strings */

  /**
   * Creates an int loadable that writes to JSON as a string, can be used as a map key.
   * @param radix  Base for conversion, base 10 is standard JSON numbers.
   */
  public StringLoadable<Integer> asString(int radix) {
    return new StringIntLoadable(min, max, radix, network);
  }


  /** Writes to a string instead of to an integer */
  private static class StringIntLoadable extends IntLoadable implements StringLoadable<Integer> {
    private final int radix;
    public StringIntLoadable(int min, int max, int radix, IntNetwork network) {
      super(min, max, network);
      if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
        throw new IllegalArgumentException("Invalid radix " + radix + ", must be between " + Character.MIN_RADIX + " and " + Character.MAX_RADIX);
      }
      this.radix = radix;
    }

    @Override
    public Integer parseString(String value, String key) {
      try {
        return validate(Integer.parseInt(value, radix), key);
      } catch (NumberFormatException e) {
        throw new JsonSyntaxException("Failed to parse integer at " + key, e);
      }
    }

    @Override
    public Integer convert(JsonElement element, String key) {
      return parseString(GsonHelper.convertToString(element, key), key);
    }

    @Override
    public String getString(Integer value) {
      return Integer.toString(value, radix);
    }

    @Override
    public JsonElement serialize(Integer value) {
      return new JsonPrimitive(getString(value));
    }
  }
}
