package mantle.router.routing.builtin

import mantle.router.routing.{MantleMessage, IPipelineUnit}
import cpw.mods.fml.common.event.FMLInterModComms

/**
 * Applied Energistics pipeline unit
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
object UnitAppEng extends IPipelineUnit {

  def unitName: String = "AppEng Compat"

  def handleMessage(msg: MantleMessage) {
    if (msg.getMessage.equals("movableTile")) {
      msg.getAttachment match {
        case s:String => FMLInterModComms.sendMessage("AppliedEnergistics", "movabletile", s)
      }
    }
  }

}
