package mantle.core.math

/**
 * Misc math utilities
 *
 * @author progwml6, Sunstrike
 */
object MathUtils {

  /**
   * Get the minimum of two Ordered objects
   *
   * @param x First object to compare
   * @param y Second object to compare
   * @tparam T An Ordered type
   * @return The lower value
   */
  def min[T <% Ordered[T]](x:T, y:T):T = if (x < y) x else y

  /**
   * Get the maximum of two Ordered objects
   *
   * @param x First object to compare
   * @param y Second object to compare
   * @tparam T An Ordered type
   * @return The higher value
   */
  def max[T <% Ordered[T]](x:T, y:T):T = if (x > y) x else y

}
