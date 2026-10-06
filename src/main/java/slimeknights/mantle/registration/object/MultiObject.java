package slimeknights.mantle.registration.object;

import net.minecraft.core.Holder;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/** Shared class for objects containing multiple other objects */
public interface MultiObject<T> {
  /** {@return list of objects in this multiobject} */
  List<T> values();

  /** {@return stream of objects in this multiobject} */
  default Stream<T> stream() {
    return values().stream();
  }

  /** Runs the consumer on each element in the object */
  default void forEach(Consumer<? super T> consumer) {
    values().forEach(consumer);
  }

  /** Multiobject containing holders */
  interface Holders<T> extends MultiObject<T> {
    /** {@return list of holders in this multiobject} */
    Collection<Holder<T>> holders();

    @Override
    default Stream<T> stream() {
      return holders().stream().filter(Holder::isBound).map(Holder::value);
    }

    @Override
    default List<T> values() {
      return stream().toList();
    }

    @Override
    default void forEach(Consumer<? super T> consumer) {
      stream().forEach(consumer);
    }
  }
}
