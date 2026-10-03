package slimeknights.mantle.data.loadable.primitive;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.array.ArrayLoadable;
import slimeknights.mantle.data.loadable.array.DoubleArrayLoadable;
import slimeknights.mantle.util.typed.TypedMap;

/**
 * Loadable for a float
 * @param min  Minimum allowed value
 * @param max  Maximum allowed value
 */
@SuppressWarnings("unused")  // API
public record DoubleLoadable(double min, double max) implements Loadable<Double> {
  /** Loadable ranging from negative infinity to positive infinity */
  public static final DoubleLoadable ANY = range(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
  /** Loadable ranging from 0 to positive infinity */
  public static final DoubleLoadable FROM_ZERO = min(0);
  /** Loadable ranging from 0 to 1 */
  public static final DoubleLoadable PERCENT = range(0, 1);

  /** Creates a loadable with the given range */
  public static DoubleLoadable range(double min, double max) {
    return new DoubleLoadable(min, max);
  }

  /** Creates a loadable ranging from the parameter to short max */
  public static DoubleLoadable min(double min) {
    return new DoubleLoadable(min, Double.POSITIVE_INFINITY);
  }

  private double validate(double value, String key) {
    if (min <= value && value <= max) {
      return value;
    }
    if (min == Double.NEGATIVE_INFINITY) {
      throw new JsonSyntaxException(key + " must not be greater than " + max);
    }
    if (max == Double.POSITIVE_INFINITY) {
      throw new JsonSyntaxException(key + " must not be less than " + min);
    }
    throw new JsonSyntaxException(key + " must be between " + min + " and " + max);
  }

  @Override
  public Double convert(JsonElement element, String key, TypedMap context) {
    return validate(GsonHelper.convertToDouble(element, key), key);
  }

  @Override
  public Double decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return buffer.readDouble();
  }

  @Override
  public JsonElement serialize(Double object) {
    return new JsonPrimitive(validate(object, "Value"));
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, Double object) {
    buffer.writeDouble(object);
  }

  /** Creates a loadable for a float array */
  public ArrayLoadable<double[]> array(int minSize, int maxSize) {
    return new DoubleArrayLoadable(this, minSize, maxSize);
  }

  /** Creates a loadable for a float array */
  public ArrayLoadable<double[]> array(int minSize) {
    return array(minSize, Integer.MAX_VALUE);
  }
}
