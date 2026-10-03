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

/** Loadable for a character array */
public record CharArrayLoadable(Loadable<Character> base, int minSize, int maxSize) implements ArrayLoadable.SizeRange<char[]> {
  @Override
  public int getLength(char[] array) {
    return array.length;
  }

  @Override
  public char[] convertCompact(JsonElement element, String key, TypedMap context) {
    return new char[] { base.convert(element, key, context) };
  }

  @Override
  public char[] convertArray(JsonArray array, String key, TypedMap context) {
    char[] result = new char[array.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = base.convert(array.get(i), key + '[' + i + ']', context);
    }
    return result;
  }

  @Override
  public JsonElement serializeFirst(char[] object) {
    return base.serialize(object[0]);
  }

  @Override
  public void serializeAll(JsonArray array, char[] object) {
    for (char element : object) {
      array.add(base.serialize(element));
    }
  }

  @Override
  public char[] decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    int max = buffer.readVarInt();
    char[] array = new char[max];
    for (int i = 0; i < max; i++) {
      array[i] = base.decode(buffer, context);
    }
    return array;
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, char[] array) {
    buffer.writeVarInt(array.length);
    for (char element : array) {
      base.encode(buffer, element);
    }
  }

  @Override
  public <P> LoadableField<char[],P> defaultField(String key, char[] defaultValue, boolean serializeDefault, Function<P,char[]> getter) {
    //noinspection Convert2Diamond  I think the method overloading stops type inferrence here
    return new DefaultingField<char[],P>(this, key, defaultValue, serializeDefault ? null : Arrays::equals, getter);
  }

  @Override
  public <P> LoadableField<char[],P> emptyField(String key, boolean serializeEmpty, Function<P,char[]> getter) {
    return defaultField(key, new char[0], serializeEmpty, getter);
  }
}
