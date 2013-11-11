package mantle.compat.minecraft.crash

import cpw.mods.fml.common.ICrashCallable

/**
 * ICrashCallable for mods to report a supported environment during crashes
 *
 * @param modLabel The name of the mod creating the callable
 * @param message A message to show this is a supported environment e.g. "Environment clean."
 */
class CallableSuppConfig(modLabel:String, message:String) extends ICrashCallable {

  def getLabel = modLabel

  def call = message

}