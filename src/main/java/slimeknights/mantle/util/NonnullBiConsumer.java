package slimeknights.mantle.util;

import org.jetbrains.annotations.Nonnull;
import java.util.function.BiConsumer;

/**
 * Equivalent to {@link BiConsumer}, except with nonnull contract.
 *
 * @see BiConsumer
 */
@FunctionalInterface
public interface NonnullBiConsumer<T,U> {
  void accept(@Nonnull T t, @Nonnull U u);
}
