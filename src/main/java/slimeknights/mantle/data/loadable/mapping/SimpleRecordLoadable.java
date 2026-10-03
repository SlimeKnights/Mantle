package slimeknights.mantle.data.loadable.mapping;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;

/**
 * Implements a record loadable with a single key.
 * @param loadable      Loadable for parsing
 * @param key           Key used in object form
 * @param defaultValue  If non-null, will be used as the value if the object is empty.
 * @param compact       If true, serializes using the loadable instead of in object form.
 */
public record SimpleRecordLoadable<T>(Loadable<T> loadable, String key, @Nullable T defaultValue, boolean compact) implements RecordLoadable<T> {
  @Override
  public T convert(JsonElement element, String key, TypedMap context) {
    if (!element.isJsonObject()) {
      return loadable.convert(element, key, context);
    }
    return RecordLoadable.super.convert(element, key, context);
  }

  @Override
  public T deserialize(JsonObject json, TypedMap context) {
    if (defaultValue != null) {
      return loadable.getOrDefault(json, key, defaultValue, context);
    } else {
      return loadable.getIfPresent(json, key, context);
    }
  }

  @Override
  public JsonElement serialize(T object) {
    if (compact) {
      return loadable.serialize(object);
    }
    return RecordLoadable.super.serialize(object);
  }

  @Override
  public void serialize(T object, JsonObject json) {
    json.add(key, loadable.serialize(object));
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, T value) {
    loadable.encode(buffer, value);
  }

  @Override
  public T decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return loadable.decode(buffer, context);
  }
}
