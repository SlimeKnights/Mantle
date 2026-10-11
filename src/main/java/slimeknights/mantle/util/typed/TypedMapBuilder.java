package slimeknights.mantle.util.typed;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.NoArgsConstructor;
import slimeknights.mantle.util.typed.TypedMap.Key;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/** Builder for a typed map, ensures key value pairs are consistent */
@SuppressWarnings("unused")  // API
@NoArgsConstructor(staticName = "builder")
public class TypedMapBuilder {
  /** Empty consumer for {@link slimeknights.mantle.data.codec.LoadableCodec}, {@link slimeknights.mantle.data.codec.LoadableMapCodec} and {@link slimeknights.mantle.network.codec.StreamableCodec}. */
  public static Consumer<TypedMapBuilder> EMPTY_CONSUMER = builder -> {};

  private final Map<Key<?>,Object> builder = new HashMap<>();

  /** Adds all values from the passed map to this map. */
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
    if (builder.isEmpty()) {
      return TypedMap.EMPTY;
    }
    return new BackedTypedMap(Map.copyOf(builder));
  }
}
