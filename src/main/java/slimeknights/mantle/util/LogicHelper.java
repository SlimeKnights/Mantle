package slimeknights.mantle.util;

import javax.annotation.Nullable;
import java.util.List;

public class LogicHelper {
  private LogicHelper() {}

  /**
   * Replaces check with a default value if null
   * @param check      Value to check
   * @param fallback   Fallback to return if the value is undesired
   * @param <T>  Type of data
   * @return  Value if not undesired, fallback
   */
  public static <T> T defaultIfNull(@Nullable T check, T fallback) {
    if (check == null) {
      return fallback;
    }
    return check;
  }

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
  public static <E> E getOrDefault(List<E> list, int index, E defaultValue) {
    if (index < 0 || index > list.size()) {
      return defaultValue;
    }
    return list.get(index);
  }
}
