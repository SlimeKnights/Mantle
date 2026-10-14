package slimeknights.mantle.util;

import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;
import java.util.List;

public class LogicHelper {
  private LogicHelper() {}

  /**
   * Replaces check with a default value if null
   * @param check      Value to check
   * @param undesired  Undesired value
   * @param fallback   Fallback to return if the value is equal to undesired
   * @return  Value or fallback
   */
  public static int defaultIf(int check, int undesired, int fallback) {
    if (check == undesired) {
      return fallback;
    }
    return check;
  }

  /**
   * Gets a value from the list, or a default if the index is out of range
   * @param list          List
   * @param index         Index to fetch
   * @param defaultValue  Value if the index is out of range
   * @param <E>  List type
   * @return  List value or default
   */
  @Nullable
  @Contract("_,_,!null->!null")
  public static <E> E getOrDefault(List<E> list, int index, @Nullable E defaultValue) {
    if (index < 0 || index >= list.size()) {
      return defaultValue;
    }
    return list.get(index);
  }

  /** Quick helper to search an array for a given value by reference equality, uses {@link Object#equals(Object)} for comparisons. */
  public static <T> boolean isInList(T[] slots, T predicate) {
    for (T slot : slots) {
      if (predicate.equals(slot)) {
        return true;
      }
    }
    return false;
  }

  /** Resolves a lazy optional, returning null if absent. Exists as the base method isn't properly annotated. */
  @SuppressWarnings("DataFlowIssue")
  @Nullable
  public static <T> T orElseNull(LazyOptional<T> optional) {
    return optional.orElse(null);
  }
}
