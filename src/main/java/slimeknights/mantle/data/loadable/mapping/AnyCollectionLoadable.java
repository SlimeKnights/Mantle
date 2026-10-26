package slimeknights.mantle.data.loadable.mapping;

import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.field.LoadableField;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Collection loadable that does not declare a specific internal collection type, meaning we can serialize from any */
public class AnyCollectionLoadable<T> extends CollectionLoadable<T,Collection<T>> {
  private final Function<Collection<T>,Collection<T>> builder;
  public AnyCollectionLoadable(Loadable<T> base, int minSize, Function<Collection<T>,Collection<T>> builder) {
    super(base, minSize);
    this.builder = builder;
  }

  /** Creates a list backed collection loadable */
  public static <T> AnyCollectionLoadable<T> listBacked(Loadable<T> base, int minSize) {
    return new AnyCollectionLoadable<>(base, minSize, List::copyOf);
  }

  /** Creates a set backed collection loadable */
  public static <T> AnyCollectionLoadable<T> setBacked(Loadable<T> base, int minSize) {
    return new AnyCollectionLoadable<>(base, minSize, Set::copyOf);
  }

  @Override
  protected Collection<T> build(Collection<T> builder) {
    return this.builder.apply(builder);
  }

  /** Creates a map from this collection using the given getter to find keys for the map */
  public <K> Loadable<Map<K,T>> mapWithKeys(Function<T,K> keyGetter) {
    return map(collection -> collection.stream().collect(Collectors.toUnmodifiableMap(keyGetter, Function.identity())), Map::values);
  }

  /** Creates a map from this collection using the given getter to find values for the map */
  public <V> Loadable<Map<T,V>> mapWithValues(Function<T,V> valueGetter) {
    return map(collection -> collection.stream().collect(Collectors.toUnmodifiableMap(Function.identity(), valueGetter)), Map::keySet);
  }

  @Override
  public <P> LoadableField<Collection<T>,P> emptyField(String key, boolean serializeEmpty, Function<P,Collection<T>> getter) {
    return defaultField(key, List.of(), serializeEmpty, getter);
  }
}
