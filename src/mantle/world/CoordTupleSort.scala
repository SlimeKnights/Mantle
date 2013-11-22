package mantle.world

import java.util.Comparator

/**
 * CoordTupleSort
 *
 * @author mDiyo, Sunstrike
 */
class CoordTupleSort extends Comparator[CoordTuple] {

  override def compare(o1: CoordTuple, o2: CoordTuple): Int = {
    if (o1.y != o2.y) return o1.y - o2.y
    if (o1.x != o2.x) return o1.x - o2.x
    if (o1.z != o2.z) return o1.z - o2.z
    0
  }

}

