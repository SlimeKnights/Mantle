package slimeknights.mantle.data.loadable.mapping;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;

/**
 * Implements a record loadable with a single key.
 * @param key           Key used in object form
 * @param loadable      Loadable for parsing
 * @param compact       If true, serializes using the loadable instead of in object form.
 * @param defaultValue  If non-null, will be used as the value if the object is empty.
 */
public record SimpleRecordLoadable<T>(String key, Loadable<T> loadable, boolean compact, @Nullable T defaultValue) implements RecordLoadable<T> {
  @Override
  public T convert(JsonElement element, String key) {
    if (!element.isJsonObject()) {
      return loadable.convert(element, key);
    }
    return RecordLoadable.super.convert(element, key);
  }

  @Override
  public T deserialize(JsonObject json, TypedMap context) {
    if (defaultValue != null) {
      return loadable.getOrDefault(json, key, defaultValue);
    } else {
      return loadable.getIfPresent(json, key);
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
  public void encode(FriendlyByteBuf buffer, T value) {
    loadable.encode(buffer, value);
  }

  @Override
  public T decode(FriendlyByteBuf buffer, TypedMap context) {
    return loadable.decode(buffer);
  }
}
