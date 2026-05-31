package slimeknights.mantle.registration.adapter;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.minecraft.core.Registry;

@SuppressWarnings("unused")
public class ContainerTypeRegistryAdapter extends RegistryAdapter<MenuType<?>> {
  /** @inheritDoc */
  public ContainerTypeRegistryAdapter(Registry<MenuType<?>> registry, String modId) {
    super(registry, modId);
  }

  /** @inheritDoc */
  public ContainerTypeRegistryAdapter(Registry<MenuType<?>> registry) {
    super(registry);
  }

  /**
   * Registers a container type
   * @param name     Container name
   * @param factory  Container factory
   * @param <C>      Container type
   * @return  Registry object containing the container type
   */
  public <C extends AbstractContainerMenu> MenuType<C> registerType(IContainerFactory<C> factory, String name) {
    return register(IMenuTypeExtension.create(factory), name);
  }
}
