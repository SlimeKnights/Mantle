package slimeknights.mantle.registration.deferred;

import net.minecraft.core.Registry;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.RegistryObject;

/**
 * Deferred register for menu types, automatically mapping a factory argument in {@link IForgeMenuType}
 */
@SuppressWarnings("unused")
public class MenuTypeDeferredRegister extends DeferredRegisterWrapper<MenuType<?>> {

  public MenuTypeDeferredRegister(String modID) {
    super(Registry.MENU_REGISTRY, modID);
  }

  /**
   * Registers a container type
   * @param name     Container name
   * @param factory  Container factory
   * @param <C>      Container type
   * @return  Registry object containing the container type
   */
  public <C extends AbstractContainerMenu> RegistryObject<MenuType<C>> register(String name, IContainerFactory<C> factory) {
    return register.register(name, () -> IForgeMenuType.create(factory));
  }
}
