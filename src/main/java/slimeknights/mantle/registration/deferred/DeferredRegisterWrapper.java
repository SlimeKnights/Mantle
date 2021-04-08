package slimeknights.mantle.registration.deferred;

import net.minecraft.util.Identifier;

@SuppressWarnings("WeakerAccess")
public abstract class DeferredRegisterWrapper{
  /** Mod ID for registration */
  private final String modID;
  /* Utilities */

  public DeferredRegisterWrapper(String modID) {
    this.modID = modID;
  }

  /**
   * Gets a resource location object for the given name
   * @param name  Name
   * @return  Resource location string
   */
  protected Identifier resource(String name) {
    return new Identifier(modID, name);
  }

  /**
   * Gets a resource location string for the given name
   * @param name  Name
   * @return  Resource location string
   */
  protected String resourceName(String name) {
    return modID + ":" + name;
  }
}
