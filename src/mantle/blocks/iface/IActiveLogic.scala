package mantle.blocks.iface

/**
 * Marks blocks that can be active and inactive.
 *
 * @author mDiyo
 * @author Sunstrike
 */
trait IActiveLogic {

  def getActive: Boolean

  def setActive(flag: Boolean)

}
