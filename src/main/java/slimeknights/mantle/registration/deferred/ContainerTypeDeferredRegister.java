package slimeknights.mantle.registration.deferred;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

/**
 * Deferred register for container types, automatically mapping a factory argument in IForgeContainerType
 */
@SuppressWarnings("unused")
public class ContainerTypeDeferredRegister extends DeferredRegisterWrapper {

  public ContainerTypeDeferredRegister(String modID) {
    super(modID);
  }

  /**
   * Registers a container type
   * @param <C>      Container type
   * @param name     Container name
   * @param factory  Container factory
   * @return  Registry object containing the container type
   */
  public <C extends ScreenHandler> ScreenHandlerType<C> register(Identifier name, ScreenHandlerRegistry.SimpleClientHandlerFactory<C> factory) {
    return ScreenHandlerRegistry.registerSimple(name, factory);
  }
}
