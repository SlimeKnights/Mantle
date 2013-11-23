package mantle.internal.crash

import cpw.mods.fml.common.ICrashCallable

/**
 * Crash Callable for unsupported environments.
 *
 * @param modIds List of unsupported IDs.
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
class CallableUnsuppConfig(modIds:List[String]) extends ICrashCallable {

   def getLabel = "Mantle Env"

   def call = {
     val bldr = new StringBuilder
     bldr.append("Unsupported environment; found ")
     var first = true

     for (i <- modIds) {
       if (first) {
         bldr.append(i)
         first = false
       } else bldr.append(s", $i")
     }

     bldr.result()
   }

 }
