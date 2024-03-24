package slimeknights.mantle.util.typed;

import com.google.common.collect.ImmutableMap;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;
import slimeknights.mantle.util.typed.TypedMap.Key;

import java.util.Map;

/** Builder for a typed map, ensures key value pairs are consistent */
@NoArgsConstructor(staticName = "builder")
public class TypedMapBuilder<T> {
  private final ImmutableMap.Builder<Key<? extends T>,T> builder = ImmutableMap.builder();

  /** Adds a value to the map */
  @Contract("_,_->this")
  public <K extends T> TypedMapBuilder<T> put(Key<K> key, K value) {
    builder.put(key, value);
    return this;
  }

  /** Builds the final map */
  public TypedMap<T> build() {
    Map<Key<? extends T>,T> map = builder.build();
    if (map.isEmpty()) {
      return TypedMap.empty();
    }
    return new BackedTypedMap<>(map);
  }
}
