package mantle.router.routing.builtin

import mantle.router.routing.{MantleMessage, IPipelineUnit}
import mantle.router.lib.RouterRepo._
import cpw.mods.fml.common.event.FMLInterModComms

/**
 * Buildcraft Transport pipeline unit
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
object UnitBCTransport extends IPipelineUnit {
  /**
   * Getter for the name of this unit
   *
   * @example def unitName: String = "FMP Compat"
   * @return The name of this unit (something descriptive)
   */
  def unitName = "BC Transport Compat"

  /**
   * Handles an incoming Mantle message
   *
   * @param msg The Mantle message from Router.
   */
  def handleMessage(msg: MantleMessage) {
    if (msg.getMessage.equals("registerDecorativeBlock")) {
      msg.getAttachment match {
        case (bID:Int, meta:Int) => sendBCFacadeMessage(bID, meta)
        case il:Array[Int]       => try { sendBCFacadeMessage(il(0), il(1)) } catch { case e:Exception => logWarn("Invalid Int array received.") }
      }
    }
  }

  private def sendBCFacadeMessage(bID: Int, meta: Int) {
    FMLInterModComms.sendMessage("BuildCraft|Transport", "add-facade", s"$bID@$meta")
  }

  private def logWarn(str:String) {
    logger.warning(s"[$unitName] $str")
  }

}
