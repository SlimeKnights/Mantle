package slimeknights.mantle.data.loadable.mapping;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/**
 * Loadable for a map type.
 * @param <K>  Key type
 * @param <V>  Value type
 */
public class MapLoadable<K, V> implements Loadable<Map<K,V>> {
  protected final StringLoadable<K> keyLoadable;
  protected final Loadable<V> valueLoadable;
  protected final int minSize;

  /**
   * Creates a new map loadable
   * @param keyLoadable    Loadable for the map keys, parsed from strings
   * @param valueLoadable  Loadable for map values, parsed from elements
   * @param minSize        Minimum size for the map to be valid
   */
  public MapLoadable(StringLoadable<K> keyLoadable, Loadable<V> valueLoadable, int minSize) {
    this.keyLoadable = keyLoadable;
    this.valueLoadable = valueLoadable;
    this.minSize = minSize;
  }

  /** Builds the final map, given the passed mutable map. */
  protected Map<K,V> build(Map<K,V> builder) {
    return Map.copyOf(builder);
  }

  /** Creates a new builder instance for the given expected size */
  protected Map<K,V> createBuilder(int size) {
    return new HashMap<>(size);
  }

  @Override
  public Map<K,V> convert(JsonElement element, String key, TypedMap context) {
    JsonObject json = GsonHelper.convertToJsonObject(element, key);
    if (json.size() < minSize) {
      throw new JsonSyntaxException(key + " must have at least " + minSize + " elements");
    }
    Map<K,V> builder = createBuilder(json.size());
    String mapKey = key + "'s key";
    for (Entry<String, JsonElement> entry : json.entrySet()) {
      String entryKey = entry.getKey();
      builder.put(
        keyLoadable.parseString(entryKey, mapKey, context),
        valueLoadable.convert(entry.getValue(), entryKey, context));
    }
    return build(builder);
  }

  @Override
  public JsonElement serialize(Map<K,V> map) {
    if (map.size() < minSize) {
      throw new RuntimeException("Collection must have at least " + minSize + " elements");
    }
    JsonObject json = new JsonObject();
    for (Entry<K,V> entry : map.entrySet()) {
      json.add(
        keyLoadable.getString(entry.getKey()),
        valueLoadable.serialize(entry.getValue()));
    }
    return json;
  }

  @Override
  public Map<K,V> decode(FriendlyByteBuf buffer, TypedMap context) {
    int size = buffer.readVarInt();
    Map<K,V> builder = createBuilder(size);
    for (int i = 0; i < size; i++) {
      builder.put(
        keyLoadable.decode(buffer, context),
        valueLoadable.decode(buffer, context));
    }
    return build(builder);
  }

  @Override
  public void encode(FriendlyByteBuf buffer, Map<K,V> map) {
    buffer.writeVarInt(map.size());
    for (Entry<K,V> entry : map.entrySet()) {
      keyLoadable.encode(buffer, entry.getKey());
      valueLoadable.encode(buffer, entry.getValue());
    }
  }

  /** Creates a field that defaults to empty */
  public <P> LoadableField<Map<K,V>,P> emptyField(String key, boolean serializeEmpty, Function<P,Map<K,V>> getter) {
    return defaultField(key, Map.of(), serializeEmpty, getter);
  }

  /** Creates a field that defaults to empty */
  public <P> LoadableField<Map<K,V>,P> emptyField(String key, Function<P,Map<K,V>> getter) {
    return emptyField(key, false, getter);
  }

  @Override
  public String toString() {
    return "MapLoadable[keyLoadable=" + keyLoadable + ", " +
           "valueLoadable=" + valueLoadable + ", " +
           "minSize=" + minSize + ']';
  }
}
