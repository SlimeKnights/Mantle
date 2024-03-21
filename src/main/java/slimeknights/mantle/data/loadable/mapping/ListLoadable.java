package slimeknights.mantle.data.loadable.mapping;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import slimeknights.mantle.data.loadable.Loadable;

import java.util.List;

/** Loadable of list of elements */
public class ListLoadable<T> extends CollectionLoadable<T,List<T>,Builder<T>> {
  public ListLoadable(Loadable<T> base, int minSize) {
    super(base, minSize);
  }

  @Override
  protected Builder<T> makeBuilder() {
    return ImmutableList.builder();
  }

  @Override
  protected List<T> build(Builder<T> builder) {
    return builder.build();
  }
}
