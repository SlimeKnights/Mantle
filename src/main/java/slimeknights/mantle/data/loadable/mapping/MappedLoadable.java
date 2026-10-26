package slimeknights.mantle.data.loadable.mapping;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.function.BiFunction;

/**
 * Represents a trivially mapped loadable that uses another loadable internally.
 * @param <F>  Base loadable type.
 * @param <T>  Result type after mapping.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class MappedLoadable<F,T> implements Loadable<T> {
  private final Loadable<F> base;
  protected final BiFunction<? super F,ErrorFactory,? extends T> constructor;
  protected final BiFunction<? super T,ErrorFactory,? extends F> getter;

  /** Creates a new loadable for a non-record loadable */
  public static <T,F> Loadable<T> of(Loadable<F> base, BiFunction<? super F,ErrorFactory,? extends T> constructor, BiFunction<? super T,ErrorFactory,? extends F> getter) {
    return new MappedLoadable<>(base, constructor, getter);
  }

  /** Creates a new loadable for a record loadable */
  public static <T,F> RecordLoadable<T> of(RecordLoadable<F> base, BiFunction<? super F,ErrorFactory,? extends T> constructor, BiFunction<? super T,ErrorFactory,? extends F> getter) {
    return new Record<>(base, constructor, getter);
  }

  /** Creates a new loadable for a record loadable */
  public static <T,F> StringLoadable<T> of(StringLoadable<F> base, BiFunction<? super F,ErrorFactory,? extends T> constructor, BiFunction<? super T,ErrorFactory,? extends F> getter) {
    return new StringMapped<>(base, constructor, getter);
  }

  @Override
  public T convert(JsonElement element, String key, TypedMap context) {
    return constructor.apply(base.convert(element, key, context), ErrorFactory.JSON_SYNTAX_ERROR);
  }

  @Override
  public JsonElement serialize(T object, TypedMap context) {
    return base.serialize(getter.apply(object, ErrorFactory.RUNTIME), context);
  }

  @Override
  public T decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return constructor.apply(base.decode(buffer, context), ErrorFactory.DECODER_EXCEPTION);
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, T object, TypedMap context) {
    base.encode(buffer, getter.apply(object, ErrorFactory.ENCODER_EXCEPTION), context);
  }

  /** Implementation for records */
  private static class Record<F,T> extends MappedLoadable<F,T> implements RecordLoadable<T> {
    private final RecordLoadable<F> base;
    protected Record(RecordLoadable<F> base, BiFunction<? super F,ErrorFactory,? extends T> constructor, BiFunction<? super T,ErrorFactory,? extends F> getter) {
      super(base, constructor, getter);
      this.base = base;
    }

    @Override
    public T deserialize(JsonObject json, TypedMap context) {
      return constructor.apply(base.deserialize(json, context), ErrorFactory.JSON_SYNTAX_ERROR);
    }

    @Override
    public void serializeInto(T object, JsonObject json, TypedMap context) {
      base.serializeInto(getter.apply(object, ErrorFactory.RUNTIME), json, context);
    }

    @Override
    public T decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
      return constructor.apply(base.decode(buffer, context), ErrorFactory.DECODER_EXCEPTION);
    }
  }

  /** Implementation for strings */
  private static class StringMapped<F,T> extends MappedLoadable<F,T> implements StringLoadable<T> {
    private final StringLoadable<F> base;
    protected StringMapped(StringLoadable<F> base, BiFunction<? super F,ErrorFactory,? extends T> constructor, BiFunction<? super T,ErrorFactory,? extends F> getter) {
      super(base, constructor, getter);
      this.base = base;
    }

    @Override
    public T parseString(String value, String key, TypedMap context) {
      return constructor.apply(base.parseString(value, key, context), ErrorFactory.JSON_SYNTAX_ERROR);
    }

    @Override
    public String getString(T object, TypedMap context) {
      return base.getString(getter.apply(object, ErrorFactory.RUNTIME), context);
    }
  }
}
