package mantle.world

/**
 * CoordTuplePair
 *
 * @param a The first CoordTuple
 * @param b The second CoordTuple
 *
 * @author mDiyo, Sunstrike
 */
class CoordTuplePair(val a:CoordTuple, val b:CoordTuple) {

  def this(aX: Int, aY: Int, aZ: Int, bX: Int, bY: Int, bZ: Int) = this(new CoordTuple(aX, aY, aZ), new CoordTuple(bX, bY, bZ))

}
