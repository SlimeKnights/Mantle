package mantle.router.routing

import mantle.router.lib.RouterRepo._

/**
 * Debug logging pipeline unit
 *
 * Logs all Mantle messages to console during debug runs.
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
object UnitDebugLogger extends IPipelineUnit {

  def unitName: String = "Message Debug Unit"

  def handleMessage(msg: MantleMessage) {
    logger.info(s"[UnitDebugLogger] Recv: $msg")
  }

}
