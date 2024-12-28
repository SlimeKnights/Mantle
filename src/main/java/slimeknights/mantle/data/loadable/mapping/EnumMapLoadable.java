package slimeknights.mantle.data.loadable.mapping;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.primitive.EnumLoadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Loadable for a map type with enum keys. Allows using the more efficient enum map on parsing
 * @param <K>  Key type
 * @param <V>  Value type
 */
public class EnumMapLoadable<K extends Enum<K>,V> extends MapLoadable<K,V> {
  private final Class<K> enumClass;
  public EnumMapLoadable(Class<K> enumClass, StringLoadable<K> keyLoadable, Loadable<V> valueLoadable, int minSize) {
    super(keyLoadable, valueLoadable, minSize);
    this.enumClass = enumClass;
  }

  public EnumMapLoadable(EnumLoadable<K> keyLoadable, Loadable<V> valueLoadable, int minSize) {
    this(keyLoadable.enumClass(), keyLoadable, valueLoadable, minSize);
  }

  @Override
  public Map<K,V> convert(JsonElement element, String key) {
    JsonObject json = GsonHelper.convertToJsonObject(element, key);
    if (json.size() < minSize) {
      throw new JsonSyntaxException(key + " must have at least " + minSize + " elements");
    }
    Map<K,V> map = new EnumMap<>(enumClass);
    String mapKey = key + "'s key";
    for (Entry<String,JsonElement> entry : json.entrySet()) {
      String entryKey = entry.getKey();
      map.put(
        keyLoadable.parseString(entryKey, mapKey),
        valueLoadable.convert(entry.getValue(), entryKey));
    }
    return map;
  }

  @Override
  public Map<K,V> decode(FriendlyByteBuf buffer) {
    int size = buffer.readVarInt();
    Map<K,V> map = new EnumMap<>(enumClass);
    for (int i = 0; i < size; i++) {
      map.put(
        keyLoadable.decode(buffer),
        valueLoadable.decode(buffer));
    }
    return map;
  }
}
