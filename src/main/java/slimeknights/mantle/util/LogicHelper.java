package slimeknights.mantle.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/** Helpers for basic logic, not relevant to another helper. */
@SuppressWarnings("unused")  // API
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

  /** Creates a new array by copying the first and appending all elements in the second. */
  @SafeVarargs
  public static <T> T[] append(T[] first, T... second) {
    T[] result = Arrays.copyOf(first, first.length + second.length);
    System.arraycopy(second, 0, result, first.length, second.length);
    return result;
  }

  /** Creates a new array by copying the second array and appending all elements in the first. */
  @SuppressWarnings("unchecked")
  @SafeVarargs
  public static <T> T[] prepend(T[] first, T... second) {
    T[] result = (T[]) Arrays.copyOf(second, first.length + second.length, first.getClass());
    System.arraycopy(first, 0, result, second.length, first.length);
    return result;
  }
}
