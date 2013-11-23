package mantle.internal.crash

import cpw.mods.fml.common.ICrashCallable

/**
 * Crash Callable for supported environments.
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
class CallableSuppConfig extends ICrashCallable {

  def getLabel = "Mantle Env"

  def call = "Environment healthy."

}
