package mantle.world

import java.lang.String

/**
 * DimensionCoordPair
 *
 * @param dim The dimension ID
 * @param x The x coordinate
 * @param z The z coordinate
 *
 * @author mDiyo, Sunstrike
 */
class DimensionCoordPair(val dim: Int, val x:Int, val z:Int) {

  def this(tuple: DimensionCoordPair) = this(tuple.dim, tuple.x, tuple.z)

  def equalCoords(dim: Int, posX: Int, posZ: Int): Boolean = this.dim == dim && this.x == posX && this.z == posZ

  override def hashCode: Int = {
    val prime: Int = 31
    var result: Int = 1
    result = prime * result + dim
    result = prime * result + x
    result = prime * result + z

    result
  }

  override def equals(obj: scala.Any): Boolean = obj match {
    case a:DimensionCoordPair => equalCoords(a.dim, a.x, a.z)
    case _                    => false
  }

  override def toString: String = s"Dim: $dim, X: $x, Z: $z"

}
