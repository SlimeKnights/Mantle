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

/** Loadable for an integer array */
public record LongArrayLoadable(Loadable<Long> base, int minSize, int maxSize) implements ArrayLoadable.SizeRange<long[]> {
  @Override
  public int getLength(long[] array) {
    return array.length;
  }

  @Override
  public long[] convertCompact(JsonElement element, String key, TypedMap context) {
    return new long[] { base.convert(element, key, context) };
  }

  @Override
  public long[] convertArray(JsonArray array, String key, TypedMap context) {
    long[] result = new long[array.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = base.convert(array.get(i), key + '[' + i + ']', context);
    }
    return result;
  }

  @Override
  public JsonElement serializeFirst(long[] object) {
    return base.serialize(object[0]);
  }

  @Override
  public void serializeAll(JsonArray array, long[] object) {
    for (long element : object) {
      array.add(base.serialize(element));
    }
  }

  @Override
  public long[] decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    int max = buffer.readVarInt();
    long[] array = new long[max];
    for (int i = 0; i < max; i++) {
      array[i] = base.decode(buffer, context);
    }
    return array;
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, long[] array) {
    buffer.writeVarInt(array.length);
    for (long element : array) {
      base.encode(buffer, element);
    }
  }

  @Override
  public <P> LoadableField<long[],P> defaultField(String key, long[] defaultValue, boolean serializeDefault, Function<P,long[]> getter) {
    //noinspection Convert2Diamond  I think the method overloading stops type inferrence here
    return new DefaultingField<long[],P>(this, key, defaultValue, serializeDefault ? null : Arrays::equals, getter);
  }

  @Override
  public <P> LoadableField<long[],P> emptyField(String key, boolean serializeEmpty, Function<P,long[]> getter) {
    return defaultField(key, new long[0], serializeEmpty, getter);
  }
}
