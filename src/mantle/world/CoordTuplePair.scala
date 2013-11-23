package mantle.world

/**
 * CoordTuplePair
 *
 * @param a The first CoordTuple
 * @param b The second CoordTuple
 *
 * @author mDiyo
 * @author Sunstrike <sunstrike@azurenode.net>
 */
class CoordTuplePair(val a:CoordTuple, val b:CoordTuple) {

  def this(aX: Int, aY: Int, aZ: Int, bX: Int, bY: Int, bZ: Int) = this(new CoordTuple(aX, aY, aZ), new CoordTuple(bX, bY, bZ))

}
