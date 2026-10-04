package slimeknights.mantle.data.loadable.field;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import slimeknights.mantle.util.typed.TypedMap;

/** Wrapper around a {@link LoadableField} which redirects all methods. */
public interface LoadableFieldWrapper<T,P> extends LoadableField<T, P>, RecordFieldWrapper<T, P> {
  /** Base field being wrapped */
  @Override
  LoadableField<T,P> base();

  @Override
  default String key() {
    return base().key();
  }

  @Override
  default T get(JsonObject json, String key, TypedMap context) {
    return base().get(json, key, context);
  }

  @NonExtendable
  @Override
  default T get(JsonObject json, TypedMap context) {
    return LoadableField.super.get(json, context);
  }
}
