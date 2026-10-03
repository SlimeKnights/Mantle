package slimeknights.mantle.data.loadable.array;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.field.DefaultingField;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;

/** Loadable for an object array */
public record ObjectArrayLoadable<T>(Loadable<T> base, IntFunction<T[]> constructor, int minSize, int maxSize, boolean allowNull) implements ArrayLoadable.SizeRange<T[]> {
  @Override
  public int getLength(T[] array) {
    return array.length;
  }

  /** Parses an element, handling null */
  @Nullable
  private T parseElement(JsonElement element, String key, TypedMap context) {
    if (allowNull && element.isJsonNull()) {
      return null;
    }
    return base.convert(element, key, context);
  }

  @Override
  public T[] convertCompact(JsonElement element, String key, TypedMap context) {
    T[] result = constructor.apply(1);
    result[0] = parseElement(element, key, context);
    return result;
  }

  @Override
  public T[] convertArray(JsonArray array, String key, TypedMap context) {
    T[] result = constructor.apply(array.size());
    for (int i = 0; i < result.length; i++) {
      result[i] = parseElement(array.get(i), key + '[' + i + ']', context);
    }
    return result;
  }

  /** Serializes the element to JSON, handling nulls */
  private JsonElement serializeElement(@Nullable T object, int index) {
    if (object == null) {
      if (allowNull) {
        return JsonNull.INSTANCE;
      }
      throw new NullPointerException("Received null at index " + index + " in ArrayLoadable not supporting null");
    }
    return base.serialize(object);
  }

  @Override
  public JsonElement serializeFirst(T[] object) {
    return serializeElement(object[0], 0);
  }

  @Override
  public void serializeAll(JsonArray array, T[] object) {
    for (int i = 0; i < object.length; i++) {
      array.add(serializeElement(object[i], i));
    }
  }

  @Override
  public T[] decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    int max = buffer.readVarInt();
    T[] array = constructor.apply(max);
    for (int i = 0; i < max; i++) {
      if (allowNull && !buffer.readBoolean()) {
        array[i] = null;
      } else {
        array[i] = base.decode(buffer, context);
      }
    }
    return array;
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, T[] array) {
    buffer.writeVarInt(array.length);
    for (T element : array) {
      if (allowNull) {
        buffer.writeBoolean(element != null);
      } else {
        Objects.requireNonNull(element);
      }
      if (element != null) {
        base.encode(buffer, element);
      }
    }
  }

  @Override
  public <P> LoadableField<T[],P> defaultField(String key, T[] defaultValue, boolean serializeDefault, Function<P,T[]> getter) {
    //noinspection Convert2Diamond  I think the method overloading stops type inferrence here
    return new DefaultingField<T[],P>(this, key, defaultValue, serializeDefault ? null : Arrays::equals, getter);
  }

  @Override
  public <P> LoadableField<T[],P> emptyField(String key, boolean serializeEmpty, Function<P,T[]> getter) {
    return defaultField(key, constructor.apply(0), serializeEmpty, getter);
  }
}
