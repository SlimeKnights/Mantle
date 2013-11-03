package mantle.router.routing.builtin

import mantle.router.routing.{MantleMessage, IPipelineUnit}
import mantle.router.lib.RouterRepo._
import cpw.mods.fml.common.event.FMLInterModComms

/**
 * Applied Energistics pipeline unit
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
object UnitAppEng extends IPipelineUnit {

  def unitName: String = "AppEng Compat"

  def handleMessage(msg: MantleMessage) = msg.getAttachment match {
    case s:String => FMLInterModComms.sendMessage("AppliedEnergistics", "movabletile", s)
    case _        => logger.warning(s"Received invalid movableTile message: $msg")
  }

}
