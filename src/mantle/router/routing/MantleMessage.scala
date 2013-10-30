package mantle.router.routing

/**
 * IMCMessage analogue for Mantle Router pipeline
 *
 * Instances of this are used to send messages to the Coordinator for processing through the Router pipeline.
 *
 * @param origin The mod ID the message is from
 * @param msg The message to be sent, in IMC-style form e.g. `registerDecorativeBlock`
 * @param obj (Optional) An object to attach to the message.
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
class MantleMessage(origin:String, msg:String, obj:Any) {

  private val modId = origin
  private val message = msg
  private val attachment = obj

  /**
   * Get the source mods modID.
   *
   * @return Mod ID of sender
   */
  def getOrigin = modId

  /**
   * Get the message this object contains.
   *
   * @return The message string
   */
  def getMessage = message

  /**
   * Get the object attached to the message.
   *
   * @note This must be null-checked as the field is permitted to be null at instantiation.
   * @return The attachment or null.
   */
  def getAttachment = attachment

  override def toString: String = s"(($origin) $message :: $obj)"

}
