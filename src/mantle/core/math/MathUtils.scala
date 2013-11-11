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

  /**
   * Java helper for min (Integers)
   */
  def minInt(x:Int, y:Int) = min(x, y)

  /**
   * Java helper for max (Integers)
   */
  def maxInt(x:Int, y:Int) = max(x, y)

  /**
   * Java helper for min (Floats)
   */
  def minFloat(x:Float, y:Float) = min(x, y)

  /**
   * Java helper for max (Floats)
   */
  def maxFloat(x:Float, y:Float) = max(x, y)

  /**
   * Java helper for min (Doubles)
   */
  def minDouble(x:Double, y:Double) = min(x, y)

  /**
   * Java helper for max (Doubles)
   */
  def maxDouble(x:Double, y:Double) = max(x, y)

}
