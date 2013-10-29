package mantle.router.imc

import cpw.mods.fml.common.event.FMLInterModComms.{IMCMessage, IMCEvent}
import scala.collection.mutable.MutableList

import mantle.router.lib.RouterRepo._

/**
 * Router IMC handler
 *
 * Handles all incoming IMC messages during Router init.
 *
 * Reserved IMC messages:
 * * registerDecorativeBlock (Block) -- Used for FMP/BC Facades. May be hooked by other units.
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
object IMCHandler {

  private val messagePipeline:MutableList[IIMCPipelineUnit] = MutableList()

  /**
   * Receiver for IMC events
   *
   * Pulls apart an IMCEvent object for messages and feeds them through every handler in the pipeline.
   *
   * @param evt The IMCEvent to use as a message source.
   */
  def handle(evt:IMCEvent) {
    //evt.getMessages.toArray map {msg => messagePipeline map {handler => handler.handleIMCMessage(msg.asInstanceOf[IMCMessage])}}
    logger.info("Preparing for IMC processing; Pipeline = " + messagePipeline)
    for ( msg <- evt.getMessages.toArray(new Array[IMCMessage](0)); handler <- messagePipeline ) {
      handler.handleIMCMessage(msg)
    }
    logger.info("IMC processsing completed.")
  }

  /**
   * Register a Pipeline unit
   *
   * Whenever Router receives an IMC message, it'll pass the message to all registered units.
   * This allows mods to register new units.
   *
   * @note Units must be registered in preinit or init or the Unit may miss the IMC event.
   *
   * @param unit The unit to register into the pipeline
   * @return True if successful, False if registration failed.
   */
  def registerPipelineUnit(unit:IIMCPipelineUnit):Boolean = {
    if (messagePipeline.contains(unit))
      return false

    messagePipeline += unit
    true
  }

}
