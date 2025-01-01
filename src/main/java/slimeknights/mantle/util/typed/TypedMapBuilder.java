package slimeknights.mantle.util.typed;

import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.NoArgsConstructor;
import slimeknights.mantle.util.typed.TypedMap.Key;

import java.util.Map;
import java.util.Objects;

/** Builder for a typed map, ensures key value pairs are consistent */
@NoArgsConstructor(staticName = "builder")
public class TypedMapBuilder {
  private final ImmutableMap.Builder<Key<?>,Object> builder = ImmutableMap.builder();

  /** Adds all values from the passed map to this map. */
  @CanIgnoreReturnValue
  public TypedMapBuilder putAll(TypedMap map) {
    for (Key<?> key : map.keySet()) {
      builder.put(key, Objects.requireNonNull(map.get(key)));
    }
    return this;
  }

  /** Adds a value to the map */
  @CanIgnoreReturnValue
  public <K> TypedMapBuilder put(Key<K> key, K value) {
    builder.put(key, value);
    return this;
  }

  /** Builds the final map */
  public TypedMap build() {
    Map<Key<?>,Object> map = builder.build();
    if (map.isEmpty()) {
      return TypedMap.empty();
    }
    return new BackedTypedMap(map);
  }
}
