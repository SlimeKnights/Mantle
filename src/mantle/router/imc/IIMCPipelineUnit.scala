package mantle.router.imc

import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage

/**
 * IMC pipeline unit interface
 *
 * Defines the interface for objects that can be inserted into the IMCHandler pipeline.
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
trait IIMCPipelineUnit {

  /**
   * Getter for the name of this unit
   *
   * @example def unitName: String = "FMP Compat"
   * @return The name of this unit (something descriptive)
   */
  def unitName:String

  /**
   * Handles an incoming IMC message
   *
   * @param msg The IMC message from Router.
   */
  def handleIMCMessage(msg:IMCMessage)

}