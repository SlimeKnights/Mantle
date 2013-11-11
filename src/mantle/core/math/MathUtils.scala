package mantle.core.math

/**
* Math Utilites
*
* various useful math utilites
*
* @author progwml6
*/
object MathUtils {

  /**
   * finds smaller of 2 valid int's
   * based on COFH's minI method
   * @param a int variable
   * @param b int variable
   * @return the minimum of int's a & b
   * */
  def minInt(a: Int, b: Int): Int = a < b ? a : b
  /**
   * finds smaller of 2 valid int's
   * based on COFH's maxI method
   * @param a int variable
   * @param b int variable
   * @return the maximum of int's a & b
   * */
  def maxInt(a: Int, b: Int): Int = a > b ? a : b

}
