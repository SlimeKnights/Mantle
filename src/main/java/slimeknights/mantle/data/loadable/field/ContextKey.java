package slimeknights.mantle.data.loadable.field;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.mantle.util.typed.TypedMap.Key;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

/**
 * Key for fetching properties from a loadable context. This key doubles as a record field for a required context key.
 * @param <T>  Field type
 */
@RequiredArgsConstructor
public class ContextKey<T> implements Key<T> {
  /** Context field representing the object's ID */
  public static final ContextKey<ResourceLocation> ID = new ContextKey<>("id");

  /** Name of the field, used primarily for debug */
  @Getter
  private final String name;



  @Override
  public String toString() {
    return "ContextKey('" + name + "')'";
  }


  /* Fields */
  private RecordField<T,Object> requiredField;
  private RecordField<T,Object> nullableField;

  /** Gets a field requiring this context parameter */
  public RecordField<T,Object> requiredField() {
    if (requiredField == null) {
      requiredField = new Required<>(this, (t,e) -> t);
    }
    return requiredField;
  }

  /** Gets a field requiring this context parameter, but mapped using the passed function */
  public <M> RecordField<M,Object> mappedField(BiFunction<T,ErrorFactory,M> mapper) {
    return new Required<>(this, mapper);
  }

  /** Field using null in place of this parameter if missing */
  public RecordField<T,Object> nullableField() {
    if (nullableField == null) {
      nullableField = new Defaulting<>(this, null);
    }
    return nullableField;
  }

  /** Creates a defaulting field for this key */
  public RecordField<T,Object> defaultField(T defaultValue) {
    return new Defaulting<>(this, defaultValue);
  }


  /** Field instance for making the key required */
  private record Required<T,M>(ContextKey<T> key, BiFunction<T,ErrorFactory,M> mapper) implements ContextField<M> {
    @Override
    public M get(TypedMap context, ErrorFactory error) {
      T value = context.get(key);
      if (value != null) {
        return mapper.apply(value, error);
      }
      throw error.create("Unable to fetch " + key.name + " from context, this usually implements a broken JSON deserializer");
    }
  }

  /** Field instance that defaults to a given value */
  private record Defaulting<T>(ContextKey<T> key, @Nullable T defaultValue) implements ContextField<T> {
    @Override
    public T get(TypedMap context, ErrorFactory error) {
      // it is potentially faster to use get over getOrDefault so call it directly if we have no default value
      if (defaultValue == null) {
        return context.get(key);
      }
      return context.getOrDefault(key, defaultValue);
    }
  }
}
