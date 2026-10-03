package slimeknights.mantle.data.loadable.array;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.field.DefaultingField;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.Arrays;
import java.util.function.Function;

/** Loadable for a float array */
public record FloatArrayLoadable(Loadable<Float> base, int minSize, int maxSize) implements ArrayLoadable.SizeRange<float[]> {
  @Override
  public int getLength(float[] array) {
    return array.length;
  }

  @Override
  public float[] convertCompact(JsonElement element, String key, TypedMap context) {
    return new float[] { base.convert(element, key, context) };
  }

  @Override
  public float[] convertArray(JsonArray array, String key, TypedMap context) {
    float[] result = new float[array.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = base.convert(array.get(i), key + '[' + i + ']', context);
    }
    return result;
  }

  @Override
  public JsonElement serializeFirst(float[] object) {
    return base.serialize(object[0]);
  }

  @Override
  public void serializeAll(JsonArray array, float[] object) {
    for (float element : object) {
      array.add(base.serialize(element));
    }
  }

  @Override
  public float[] decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    int max = buffer.readVarInt();
    float[] array = new float[max];
    for (int i = 0; i < max; i++) {
      array[i] = base.decode(buffer, context);
    }
    return array;
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, float[] array) {
    buffer.writeVarInt(array.length);
    for (float element : array) {
      base.encode(buffer, element);
    }
  }

  @Override
  public <P> LoadableField<float[],P> defaultField(String key, float[] defaultValue, boolean serializeDefault, Function<P,float[]> getter) {
    //noinspection Convert2Diamond  I think the method overloading stops type inferrence here
    return new DefaultingField<float[],P>(this, key, defaultValue, serializeDefault ? null : Arrays::equals, getter);
  }

  @Override
  public <P> LoadableField<float[],P> emptyField(String key, boolean serializeEmpty, Function<P,float[]> getter) {
    return defaultField(key, new float[0], serializeEmpty, getter);
  }
}
