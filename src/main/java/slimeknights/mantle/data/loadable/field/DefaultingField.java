package slimeknights.mantle.data.loadable.field;

import com.google.common.base.Objects;
import com.google.gson.JsonObject;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Optional field with a default value if missing
 * @param <P>  Parent object
 * @param <T>  Loadable type
 */
public record DefaultingField<T,P>(Loadable<T> loadable, String key, T defaultValue, @Nullable BiPredicate<? super T,? super T> skipSerialize, Function<P,T> getter) implements AlwaysPresentLoadableField<T,P> {
  /** Default predicate for a defaulting field */
  private static final BiPredicate<Object,Object> DEFAULT_SKIP = Objects::equal;

  public DefaultingField(Loadable<T> loadable, String key, T defaultValue, boolean serializeDefault, Function<P,T> getter) {
    this(loadable, key, defaultValue, serializeDefault ? null : DEFAULT_SKIP, getter);
  }

  /** {@return true if the default field is always serialized} */
  public boolean serializeDefault() {
    return skipSerialize == null;
  }

  @Override
  public T get(JsonObject json, String key, TypedMap context) {
    return loadable.getOrDefault(json, key, defaultValue, context);
  }

  @Override
  public void serializeInto(P parent, JsonObject json, TypedMap context) {
    T object = getter.apply(parent);
    if (skipSerialize == null || !skipSerialize.test(defaultValue, object)) {
      json.add(key, loadable.serialize(object, context));
    }
  }
}
