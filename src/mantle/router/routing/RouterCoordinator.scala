package mantle.router.routing

import cpw.mods.fml.common.event.FMLInterModComms.{IMCMessage, IMCEvent}
import scala.collection.mutable.{MutableList, Queue}

import mantle.router.lib.RouterRepo._

/**
 * Router IMC handler
 *
 * Handles all incoming Mantle messages and their processing.
 *
 * Reserved IMC messages:
 * * registerDecorativeBlock (Block) -- Used for FMP/BC Facades. May be hooked by other units.
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
object RouterCoordinator {

  private val messagePipeline:MutableList[IPipelineUnit] = MutableList()
  private val messageQueue:Queue[MantleMessage] = Queue[MantleMessage]()

  /**
   * Send a message
   *
   * Adds a given MantleMessage to the message queue to be processed next round.
   *
   * @note This should be completed before Mantle-Router init!
   * @param msg The MantleMessage to queue.
   */
  def queueMessage(msg:MantleMessage) {
    debug(s"Queue accept: $msg")
    messageQueue.enqueue(msg)
  }

  /**
   * Processing hook
   *
   * Runs all current events through the pipeline. This is called at Touter preinit, init and postinit.
   *
   * @note This should be considered an internal call; other mods should not call this!
   */
  def processQueue() {
    debug(s"Preparing for message processing; pipeline = $messagePipeline, queue: $messageQueue")
    for ( msg <- messageQueue; handler <- messagePipeline ) {
      handler.handleMessage(msg)
    }

    messageQueue.clear()
    debug("Message processsing completed.")
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
  def registerPipelineUnit(unit:IPipelineUnit):Boolean = {
    if (messagePipeline.contains(unit))
      return false

    messagePipeline += unit
    true
  }

  /**
   * Internal log helper
   *
   * Logs input (with prepended tag) only if message system debug flag (MANTLE_MSG_SYS) is active.
   *
   * @param msg String to log.
   */
  private def debug(msg:String) {
    if (debugMessageSys) logger.info(s"[RouterCoordinator] $msg")
  }

}
