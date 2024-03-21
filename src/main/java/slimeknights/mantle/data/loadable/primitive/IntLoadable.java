package slimeknights.mantle.data.loadable.primitive;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.loadable.Loadable;

/**
 * Loadable for an integer
 * @param min      Minimum allowed value
 * @param max      Maximum allowed value
 * @param network  Method of writing to the network
 */
public record IntLoadable(int min, int max, IntNetwork network) implements Loadable<Integer> {
  /** Loadable ranging from integer min to integer max */
  public static final IntLoadable ANY_FULL = range(Integer.MIN_VALUE, Integer.MAX_VALUE);
  /** Loadable ranging from short min to short max */
  public static final IntLoadable ANY_SHORT = range(Short.MIN_VALUE, Short.MAX_VALUE);
  /** Loadable ranging from -1 to short max */
  public static final IntLoadable FROM_MINUS_ONE = min(-1);
  /** Loadable ranging from zero to short max */
  public static final IntLoadable FROM_ZERO = min(0);
  /** Loadable ranging from one to short max */
  public static final IntLoadable FROM_ONE = min(1);

  /** Creates a loadable with defaulting networking */
  public static IntLoadable range(int min, int max) {
    return new IntLoadable(min, max, IntNetwork.recommended(min, max));
  }

  /** Creates a loadable ranging from the parameter to short max */
  public static IntLoadable min(int min) {
    return range(min, Short.MAX_VALUE);
  }

  @Override
  public Integer convert(JsonElement element, String key) {
    int value = GsonHelper.convertToInt(element, key);
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
  public JsonElement serialize(Integer object) {
    return new JsonPrimitive(object);
  }

  @Override
  public Integer fromNetwork(FriendlyByteBuf buffer) {
    return network.fromNetwork(buffer);
  }

  @Override
  public void toNetwork(Integer object, FriendlyByteBuf buffer) {
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
}
