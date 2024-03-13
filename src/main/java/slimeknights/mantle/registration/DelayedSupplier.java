package slimeknights.mantle.registration;

import lombok.Setter;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Supplier wrapper that allows setting the supplier at a later time. Basically a pretty big hack.
 * Used since fluids are wrapped in a ton of self referencing suppliers, so we make the properties with a supplier that we fill later.
 * @param <T>  Supplier type
 */
public class DelayedSupplier<T> implements Supplier<T> {
  @Setter
  @Nullable
  private Supplier<? extends T> supplier;

  @Override
  public T get() {
    if (supplier == null) {
      throw new IllegalStateException("Attempted to call DelayedSupplier::get() before the supplier was set");
    }
    return supplier.get();
  }
}
