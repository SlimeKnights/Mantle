package slimeknights.mantle.data.loadable.field;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.LegacyLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;

/**
 * Field that catches parse exceptions instead of propigating them, allowing a default value to be passed to the parent.
 * @param base        Nested field to parse.
 * @param valueOnError  Value to use if an error happens. May be null provided the nested field supports null.
 * @param <T>  Value type
 * @param <P>  Parent type
 */
@SuppressWarnings("unused")  // API
public record CatchErrorsField<T,P>(LoadableField<T,P> base, @Nullable T valueOnError) implements LoadableFieldWrapper<T,P> {
  @Override
  public T get(JsonObject json, String key, TypedMap context) {
    try {
      return base.get(json, key, context);
    } catch (JsonParseException e) {
      Mantle.logger.error("Caught error on field {}{}, substituting fallback value {}.", key(), LegacyLoadable.whileParsing(context), valueOnError, e);
      return valueOnError;
    }
  }

  // no need for decode as any errors should be dealt with on parse
}
