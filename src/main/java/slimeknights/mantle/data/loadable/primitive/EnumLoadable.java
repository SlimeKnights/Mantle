package slimeknights.mantle.data.loadable.primitive;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.array.ArrayLoadable;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.mapping.EnumMapLoadable;
import slimeknights.mantle.data.loadable.mapping.EnumSetLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** Loadable for an enum value */
public record EnumLoadable<E extends Enum<E>>(Class<E> enumClass, E[] allowedValues) implements StringLoadable<E> {
  public EnumLoadable(Class<E> enumClass) {
    this(enumClass, enumClass.getEnumConstants());
  }

  /** Creates a loadable from the given list of values */
  @SafeVarargs
  public static <E extends Enum<E>> EnumLoadable<E> of(E... values) {
    if (values.length == 0) {
      throw new IllegalArgumentException("Must have at least 1 value for an enum loadable");
    }
    return new EnumLoadable<>(values[0].getDeclaringClass(), values);
  }

  @Override
  public E parseString(String name, String key, TypedMap context) {
    for (E value : allowedValues) {
      if (value.name().toLowerCase(Locale.ROOT).equals(name)) {
        return value;
      }
    }
    throw new JsonSyntaxException("Invalid " + enumClass.getSimpleName() + " " + name);
  }

  @Override
  public String getString(E object) {
    return object.name().toLowerCase(Locale.ROOT);
  }

  @Override
  public E decode(FriendlyByteBuf buffer, TypedMap context) {
    return buffer.readEnum(enumClass);
  }

  @Override
  public void encode(FriendlyByteBuf buffer, E object) {
    buffer.writeEnum(object);
  }

  @Override
  public ArrayLoadable<Set<E>> set(int minSize) {
    return new EnumSetLoadable<>(this, minSize);
  }

  @Override
  public <V> Loadable<Map<E,V>> mapWithValues(Loadable<V> valueLoadable, int minSize) {
    return new EnumMapLoadable<>(this, valueLoadable, minSize);
  }

  @Override
  public <P> LoadableField<E, P> nullableField(String key, Function<P, E> getter) {
    return new NullableEnumField<>(this, key, getter);
  }

  /** Nullable field for an enum, which saves space by writing an out-of-bounds index for null. */
  private record NullableEnumField<E extends Enum<E>,P>(EnumLoadable<E> loadable, String key, Function<P,E> getter) implements LoadableField<E,P> {
    @Nullable
    @Override
    public E get(JsonObject json, String key, TypedMap context) {
      return loadable.getOrDefault(json, key, null, context);
    }

    @Override
    public void serialize(P parent, JsonObject json) {
      E value = getter.apply(parent);
      if (value != null) {
        json.add(key, loadable.serialize(value));
      }
    }

    @Nullable
    @Override
    public E decode(FriendlyByteBuf buffer, TypedMap context) {
      int index = buffer.readVarInt();
      E[] values = loadable.enumClass.getEnumConstants();
      // if the index is the array size, that represents null
      if (index == values.length) {
        return null;
      }
      return values[index];
    }

    @Override
    public void encode(FriendlyByteBuf buffer, P parent) {
      E value = getter.apply(parent);
      // if null, write the length of the enum values
      if (value == null) {
        buffer.writeVarInt(loadable.enumClass.getEnumConstants().length);
      } else {
        buffer.writeVarInt(value.ordinal());
      }
    }
  }
}
