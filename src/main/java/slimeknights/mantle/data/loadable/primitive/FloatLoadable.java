package slimeknights.mantle.data.loadable.primitive;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.loadable.Loadable;

/**
 * Loadable for a float
 * @param min  Minimum allowed value
 * @param max  Maximum allowed value
 */
public record FloatLoadable(float min, float max) implements Loadable<Float> {
  /** Loadable ranging from negative infinity to positive infinity */
  public static final FloatLoadable ANY = range(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
  /** Loadable ranging from 0 to positive infinity */
  public static final FloatLoadable FROM_ZERO = min(0);
  /** Loadable ranging from 0 to 1 */
  public static final FloatLoadable PERCENT = range(0, 1);

  /** Creates a loadable with the given range */
  public static FloatLoadable range(float min, float max) {
    return new FloatLoadable(min, max);
  }

  /** Creates a loadable ranging from the parameter to short max */
  public static FloatLoadable min(float min) {
    return new FloatLoadable(min, Float.POSITIVE_INFINITY);
  }

  protected float validate(float value, String key) {
    if (min <= value && value <= max) {
      return value;
    }
    if (min == Float.NEGATIVE_INFINITY) {
      throw new JsonSyntaxException(key + " must not be greater than " + max);
    }
    if (max == Float.POSITIVE_INFINITY) {
      throw new JsonSyntaxException(key + " must not be less than " + min);
    }
    throw new JsonSyntaxException(key + " must be between " + min + " and " + max);
  }

  @Override
  public Float convert(JsonElement element, String key) {
    return validate(GsonHelper.convertToFloat(element, key), key);
  }

  @Override
  public Float decode(FriendlyByteBuf buffer) {
    return buffer.readFloat();
  }

  @Override
  public JsonElement serialize(Float object) {
    return new JsonPrimitive(validate(object, "Value"));
  }

  @Override
  public void encode(FriendlyByteBuf buffer, Float object) {
    buffer.writeFloat(object);
  }
}
