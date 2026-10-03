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
public record BooleanArrayLoadable(Loadable<Boolean> base, int minSize, int maxSize) implements ArrayLoadable.SizeRange<boolean[]> {
  @Override
  public int getLength(boolean[] array) {
    return array.length;
  }

  @Override
  public boolean[] convertCompact(JsonElement element, String key, TypedMap context) {
    return new boolean[] { base.convert(element, key, context) };
  }

  @Override
  public boolean[] convertArray(JsonArray array, String key, TypedMap context) {
    boolean[] result = new boolean[array.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = base.convert(array.get(i), key + '[' + i + ']', context);
    }
    return result;
  }

  @Override
  public JsonElement serializeFirst(boolean[] object) {
    return base.serialize(object[0]);
  }

  @Override
  public void serializeAll(JsonArray array, boolean[] object) {
    for (boolean element : object) {
      array.add(base.serialize(element));
    }
  }

  @Override
  public boolean[] decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    int max = buffer.readVarInt();
    boolean[] array = new boolean[max];
    for (int i = 0; i < max; i++) {
      array[i] = base.decode(buffer, context);
    }
    return array;
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, boolean[] array) {
    buffer.writeVarInt(array.length);
    for (boolean element : array) {
      base.encode(buffer, element);
    }
  }

  @Override
  public <P> LoadableField<boolean[],P> defaultField(String key, boolean[] defaultValue, boolean serializeDefault, Function<P,boolean[]> getter) {
    //noinspection Convert2Diamond  I think the method overloading stops type inferrence here
    return new DefaultingField<boolean[],P>(this, key, defaultValue, serializeDefault ? null : Arrays::equals, getter);
  }

  @Override
  public <P> LoadableField<boolean[],P> emptyField(String key, boolean serializeEmpty, Function<P,boolean[]> getter) {
    return defaultField(key, new boolean[0], serializeEmpty, getter);
  }
}
