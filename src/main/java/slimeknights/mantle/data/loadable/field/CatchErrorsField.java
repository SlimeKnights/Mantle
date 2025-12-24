package slimeknights.mantle.data.loadable.field;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.LegacyLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;

/**
 * Field that catches parse exceptions instead of propigating them, allowing a default value to be passed to the parent.
 * @param nested        Nested field to parse.
 * @param valueOnError  Value to use if an error happens. May be null provided the nested field supports null.
 * @param <T>  Value type
 * @param <P>  Parent type
 */
public record CatchErrorsField<T,P>(LoadableField<T,P> nested, @Nullable T valueOnError) implements LoadableField<T,P> {
  @Override
  public String key() {
    return nested.key();
  }

  @Override
  public T get(JsonObject json, String key, TypedMap context) {
    try {
      return nested.get(json, key, context);
    } catch (JsonParseException e) {
      Mantle.logger.error("Caught error on field {}{}, substituting fallback value {}.", key(), LegacyLoadable.whileParsing(context), valueOnError, e);
      return valueOnError;
    }
  }

  @Override
  public void serialize(P parent, JsonObject json) {
    nested.serialize(parent, json);
  }

  @Override
  public T decode(FriendlyByteBuf buffer, TypedMap context) {
    // any errors should be dealt with on parse
    return nested.decode(buffer, context);
  }

  @Override
  public void encode(FriendlyByteBuf buffer, P parent) {
    nested.encode(buffer, parent);
  }
}
