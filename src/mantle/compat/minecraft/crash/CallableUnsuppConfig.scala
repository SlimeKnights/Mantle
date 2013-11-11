package mantle.compat.minecraft.crash

import cpw.mods.fml.common.ICrashCallable

/**
 * ICrashCallable for mods to report an unsupported environment during crashes
 *
 * @param modIds List of guilty mod IDs
 * @param modLabel The name of the mod creating the callable
 */
class CallableUnsuppConfig(modIds:List[String], modLabel:String) extends ICrashCallable {

  def getLabel = modLabel

  def call = {
    var str: String = "DO NOT REPORT THIS CRASH! Unsupported mods in environment: "
    var firstEntry: Boolean = true
    for (id <- modIds) {
      str = str + (if (firstEntry) id else ", " + id)
      firstEntry = false
    }
    str
  }

}