package slimeknights.mantle.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

/**
 * Equivalent to {@link BiConsumer}, except with nonnull contract.
 *
 * @see BiConsumer
 */
@FunctionalInterface
public interface NonnullBiConsumer<T,U> {
  void accept(@NotNull T t, @NotNull U u);
}
