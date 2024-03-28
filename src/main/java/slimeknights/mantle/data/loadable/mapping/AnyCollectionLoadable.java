package slimeknights.mantle.data.loadable.mapping;

import com.google.common.collect.ImmutableCollection.Builder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import slimeknights.mantle.data.loadable.Loadable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Collection loadable that does not declare a specific internal collection type, meaning we can serialize from any */
public class AnyCollectionLoadable<T> extends CollectionLoadable<T,Collection<T>,Builder<T>> {
  private final Supplier<Builder<T>> builder;
  public AnyCollectionLoadable(Loadable<T> base, int minSize, Supplier<Builder<T>> builder) {
    super(base, minSize);
    this.builder = builder;
  }

  /** Creates a list backed collection loadable */
  public static <T> AnyCollectionLoadable<T> listBacked(Loadable<T> base, int minSize) {
    return new AnyCollectionLoadable<>(base, minSize, ImmutableList::builder);
  }

  /** Creates a set backed collection loadable */
  public static <T> AnyCollectionLoadable<T> setBacked(Loadable<T> base, int minSize) {
    return new AnyCollectionLoadable<>(base, minSize, ImmutableSet::builder);
  }

  @Override
  protected Builder<T> makeBuilder() {
    return builder.get();
  }

  @Override
  protected Collection<T> build(Builder<T> builder) {
    return builder.build();
  }

  /** Creates a map from this collection using the given getter to find keys for the map */
  public <K> Loadable<Map<K,T>> mapWithKeys(Function<T,K> keyGetter) {
    return flatXmap(collection -> collection.stream().collect(Collectors.toUnmodifiableMap(keyGetter, Function.identity())), Map::values);
  }

  /** Creates a map from this collection using the given getter to find values for the map */
  public <V> Loadable<Map<T,V>> mapWithValues(Function<T,V> valueGetter) {
    return flatXmap(collection -> collection.stream().collect(Collectors.toUnmodifiableMap(Function.identity(), valueGetter)), Map::keySet);
  }
}
