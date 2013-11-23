package mantle.world

import java.lang.String

/**
 * CoordTuple
 *
 * @param x The x coordinate
 * @param y The y coordinate
 * @param z The z coordinate
 *
 * @author mDiyo
 * @author Sunstrike <sunstrike@azurenode.net>
 */
class CoordTuple(val x: Int, val y:Int, val z:Int) extends Comparable[CoordTuple] {

  def this(posX: Double, posY: Double, posZ: Double) = this(Math.floor(posX).asInstanceOf[Int], Math.floor(posY).asInstanceOf[Int], Math.floor(posZ).asInstanceOf[Int])

  def this(tuple: CoordTuple) = this(tuple.x, tuple.y, tuple.z)

  def equalCoords(posX: Int, posY: Int, posZ: Int): Boolean = this.x == posX && this.y == posY && this.z == posZ

  override def hashCode: Int = {
    val prime: Int = 31
    var result: Int = 1
    result = prime * result + x
    result = prime * result + y
    result = prime * result + z

    result
  }

  override def toString: String = s"X: $x, Y: $y, Z: $z"

  def compareTo(o: CoordTuple): Int = o match {
    case null         => throw new NullPointerException("Object cannot be null")
    case _ if x < o.x => -1
    case _ if x > o.x => 1
    case _ if y < o.y => -1
    case _ if y > o.y => 1
    case _ if z < o.z => -1
    case _ if z > o.z => 1
    case _            => 0
  }

}
