package slimeknights.mantle.compat.neoforged.neoforge.common.util;

import java.util.function.Function;

@FunctionalInterface
public interface NonNullFunction<T, R> extends Function<T, R> {}
