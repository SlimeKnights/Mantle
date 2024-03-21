package slimeknights.mantle.data.loadable.mapping;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import slimeknights.mantle.data.loadable.Loadable;

import java.util.Set;

/** Loadable of set of elements */
public class SetLoadable<T> extends CollectionLoadable<T,Set<T>,Builder<T>> {
  public SetLoadable(Loadable<T> base, int minSize) {
    super(base, minSize);
  }

  @Override
  protected Builder<T> makeBuilder() {
    return ImmutableSet.builder();
  }

  @Override
  protected Set<T> build(Builder<T> builder) {
    return builder.build();
  }
}
