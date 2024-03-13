package slimeknights.mantle.util;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder for creating a list by adding the last elements for the final list first
 */
public class ReversedListBuilder<E> {

  /** Store the list as a list of lists as that makes reversing easier */
  private final ImmutableList.Builder<E> unpacked = ImmutableList.builder();

  /**
   * Adds the given data to the builder. Typically its best to do collections here, you can merge them in the final building.
   * @param data  Data to add
   */
  public void add(E data) {
    unpacked.add(data);
  }

  /** Builds the list into the given consumer */
  public void build(Consumer<E> consumer) {
    List<E> unpacked = this.unpacked.build();
    for (int i = unpacked.size() - 1; i >= 0; i--) {
      consumer.accept(unpacked.get(i));
    }
  }

  /** Builds a list out of a collection builder */
  public static <E> ImmutableList<E> buildList(ReversedListBuilder<Collection<E>> builder) {
    ImmutableList.Builder<E> listBuilder = ImmutableList.builder();
    builder.build(listBuilder::addAll);
    return listBuilder.build();
  }
}
