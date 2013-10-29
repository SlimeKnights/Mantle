package mantle.router.imc

import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage

import mantle.router.lib.RouterRepo._

/**
 * Debug logging pipeline unit
 *
 * Logs all IMC messages to console during debug runs.
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
object UnitDebugLogger extends IIMCPipelineUnit {

  def unitName: String = "IMC Debug Unit"

  def handleIMCMessage(msg: IMCMessage) {
    logger.info(s"[IMC Debug Unit] Message: $msg")
  }

}
