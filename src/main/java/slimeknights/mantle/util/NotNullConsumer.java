package slimeknights.mantle.util;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface NotNullConsumer<T>
{
  void accept(@NotNull T t);
}