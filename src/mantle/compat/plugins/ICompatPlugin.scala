package mantle.compat.plugins

/**
 * Trait defining a Mantle-Compat plugin.
 *
 * Any plugins providing compatibility support for externals mods must follow this to be loadable.
 *
 * @note Do not implement any functionality in the constructor! Do your setup during preInit()
 * @author Sunstrike <sunstrike@azurenode.net>
 */
trait ICompatPlugin {

  /**
   * Mod ID this plugin should be loaded for.
   *
   * @return A mod ID string
   */
  def providedMod: String

  /**
   * Called when plugin is accepted by the manager
   *
   * Use this for any critical early-stage setup.
   */
  def accept()

  /**
   * Called during Mantle-Compat preinit
   */
  def preInit()

  /**
   * Called during Mantle-Compat init
   */
  def init()

  /**
   * Called during Mantle-Compat postinit
   */
  def postInit()

  override def toString:String = s"MantleCompatPlugin(${this.getClass.getName} :: $providedMod)"

}