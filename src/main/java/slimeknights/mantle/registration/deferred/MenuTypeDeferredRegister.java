package slimeknights.mantle.registration.deferred;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Deferred register for menu types, automatically mapping a factory argument in {@link IMenuTypeExtension}
 */
@SuppressWarnings("unused")
public class MenuTypeDeferredRegister extends DeferredRegister<MenuType<?>> {
  public MenuTypeDeferredRegister(String modID) {
    super(Registries.MENU, modID);
  }

  /**
   * Registers a container type
   * @param name     Container name
   * @param factory  Container factory
   * @param <C>      Container type
   * @return  Registry object containing the container type
   */
  public <C extends AbstractContainerMenu> DeferredHolder<MenuType<?>,MenuType<C>> register(String name, IContainerFactory<C> factory) {
    return register(name, () -> IMenuTypeExtension.create(factory));
  }
}
