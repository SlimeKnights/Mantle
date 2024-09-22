package slimeknights.mantle.data.loadable.field;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;

/**
 * Shared impl a field from context
 */
public interface ContextField<T> extends RecordField<T,Object> {
  /**
   * Gets the value from context for the given key
   * @param context  Context
   * @param error    Place to print errors for failure to fetch
   * @return  Context value, or null if the field implementation does not handle it.
   * @throws RuntimeException  If the value is missing in context
   */
  @Nullable
  T get(TypedMap context, ErrorFactory error);

  @Override
  default T get(JsonObject json, TypedMap context) {
    return get(context, ErrorFactory.JSON_SYNTAX_ERROR);
  }

  @Override
  default void serialize(Object parent, JsonObject json) {}

  @Override
  default T decode(FriendlyByteBuf buffer, TypedMap context) {
    return get(context, ErrorFactory.DECODER_EXCEPTION);
  }

  @Override
  default void encode(FriendlyByteBuf buffer, Object parent) {}
}
