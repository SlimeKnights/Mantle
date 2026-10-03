package slimeknights.mantle.data.loadable.array;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectFunction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.field.DefaultingField;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.Arrays;
import java.util.function.Function;

/** Loadable for a byte array */
public record ByteArrayLoadable<T extends Number>(Loadable<T> base, int minSize, int maxSize, Byte2ObjectFunction<T> mapper) implements ArrayLoadable.SizeRange<byte[]> {
  @Override
  public int getLength(byte[] array) {
    return array.length;
  }

  @Override
  public byte[] convertCompact(JsonElement element, String key, TypedMap context) {
    return new byte[] { base.convert(element, key, context).byteValue() };
  }

  @Override
  public byte[] convertArray(JsonArray array, String key, TypedMap context) {
    byte[] result = new byte[array.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = base.convert(array.get(i), key + '[' + i + ']', context).byteValue();
    }
    return result;
  }

  @Override
  public JsonElement serializeFirst(byte[] object) {
    return base.serialize(mapper.get(object[0]));
  }

  @Override
  public void serializeAll(JsonArray array, byte[] object) {
    for (byte element : object) {
      array.add(base.serialize(mapper.get(element)));
    }
  }

  @Override
  public byte[] decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    int max = buffer.readVarInt();
    byte[] array = new byte[max];
    for (int i = 0; i < max; i++) {
      array[i] = base.decode(buffer, context).byteValue();
    }
    return array;
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, byte[] array) {
    buffer.writeVarInt(array.length);
    for (byte element : array) {
      base.encode(buffer, mapper.get(element));
    }
  }

  @Override
  public <P> LoadableField<byte[],P> defaultField(String key, byte[] defaultValue, boolean serializeDefault, Function<P,byte[]> getter) {
    //noinspection Convert2Diamond  I think the method overloading stops type inferrence here
    return new DefaultingField<byte[],P>(this, key, defaultValue, serializeDefault ? null : Arrays::equals, getter);
  }

  @Override
  public <P> LoadableField<byte[],P> emptyField(String key, boolean serializeEmpty, Function<P,byte[]> getter) {
    return defaultField(key, new byte[0], serializeEmpty, getter);
  }
}
