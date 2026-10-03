package slimeknights.mantle.data.loadable.array;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.shorts.Short2ObjectFunction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.field.DefaultingField;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.Arrays;
import java.util.function.Function;

/** Loadable for a short array */
public record ShortArrayLoadable<T extends Number>(Loadable<T> base, int minSize, int maxSize, Short2ObjectFunction<T> mapper) implements ArrayLoadable.SizeRange<short[]> {
  @Override
  public int getLength(short[] array) {
    return array.length;
  }

  @Override
  public short[] convertCompact(JsonElement element, String key, TypedMap context) {
    return new short[] { base.convert(element, key, context).shortValue() };
  }

  @Override
  public short[] convertArray(JsonArray array, String key, TypedMap context) {
    short[] result = new short[array.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = base.convert(array.get(i), key + '[' + i + ']', context).shortValue();
    }
    return result;
  }

  @Override
  public JsonElement serializeFirst(short[] object) {
    return base.serialize(mapper.get(object[0]));
  }

  @Override
  public void serializeAll(JsonArray array, short[] object) {
    for (short element : object) {
      array.add(base.serialize(mapper.get(element)));
    }
  }

  @Override
  public short[] decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    int max = buffer.readVarInt();
    short[] array = new short[max];
    for (int i = 0; i < max; i++) {
      array[i] = base.decode(buffer, context).shortValue();
    }
    return array;
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, short[] array) {
    buffer.writeVarInt(array.length);
    for (short element : array) {
      base.encode(buffer, mapper.get(element));
    }
  }

  @Override
  public <P> LoadableField<short[],P> defaultField(String key, short[] defaultValue, boolean serializeDefault, Function<P,short[]> getter) {
    //noinspection Convert2Diamond  I think the method overloading stops type inferrence here
    return new DefaultingField<short[],P>(this, key, defaultValue, serializeDefault ? null : Arrays::equals, getter);
  }

  @Override
  public <P> LoadableField<short[],P> emptyField(String key, boolean serializeEmpty, Function<P,short[]> getter) {
    return defaultField(key, new short[0], serializeEmpty, getter);
  }
}
