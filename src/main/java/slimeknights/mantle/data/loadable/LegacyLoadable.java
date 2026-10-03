package slimeknights.mantle.data.loadable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.function.Function;

/**
 * Loadable for dealing with legacy parsing elements. For instance, a deprecated JSON field.
 * See also: {@link slimeknights.mantle.data.loadable.field.LegacyField} for a simpler but less flexible approach.
 */
@RequiredArgsConstructor
public abstract class LegacyLoadable<T> implements RecordLoadable<T> {
  protected final RecordLoadable<T> base;

  @Override
  public JsonElement serialize(T object) {
    return base.serialize(object);
  }

  @Override
  public void serialize(T object, JsonObject json) {
    base.serialize(object, json);
  }


  /* NBT */

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, T value) {
    base.encode(buffer, value);
  }

  @Override
  public T decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return base.decode(buffer, context);
  }


  /* Fields */

  @Override
  public <P> LoadableField<T, P> nullableField(String key, Function<P, T> getter) {
    return base.nullableField(key, getter);
  }

  @Override
  public <P> LoadableField<T, P> defaultField(String key, T defaultValue, boolean serializeDefault, Function<P, T> getter) {
    return base.defaultField(key, defaultValue, serializeDefault, getter);
  }

  /** Gets debug information from the given context */
  public static String whileParsing(TypedMap context) {
    String debug = context.get(ContextKey.DEBUG);
    return debug != null ? " while parsing " + debug : "";
  }


  /** Simple legacy loadable that just prints a message on deserialization */
  public static <T> RecordLoadable<T> message(RecordLoadable<T> loader, String message) {
    return new DeprecatedLoader<>(loader, message);
  }

  /** Wrapper around a record loadable where the whole loader is deprecated.  */
  private static class DeprecatedLoader<T> extends LegacyLoadable<T> {
    private final String message;
    public DeprecatedLoader(RecordLoadable<T> base, String message) {
      super(base);
      this.message = message;
    }

    @Override
    public T deserialize(JsonObject json, TypedMap context) {
      Mantle.logger.warn("Encountered Deprecated Loadable{}: {}", whileParsing(context), message);
      return base.deserialize(json, context);
    }
  }
}
