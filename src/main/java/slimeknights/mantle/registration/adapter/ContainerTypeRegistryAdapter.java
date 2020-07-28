package slimeknights.mantle.registration.adapter;

import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.IForgeRegistry;

@SuppressWarnings("unused")
public class ContainerTypeRegistryAdapter extends RegistryAdapter<ContainerType<?>> {
  /** @inheritDoc */
  public ContainerTypeRegistryAdapter(IForgeRegistry<ContainerType<?>> registry, String modId) {
    super(registry, modId);
  }

  /** @inheritDoc */
  public ContainerTypeRegistryAdapter(IForgeRegistry<ContainerType<?>> registry) {
    super(registry);
  }

  /**
   * Registers a container type
   * @param name     Container name
   * @param factory  Container factory
   * @param <C>      Container type
   * @return  Registry object containing the container type
   */
  public <C extends Container> ContainerType<C> registerType(IContainerFactory<C> factory, String name) {
    return register(IForgeContainerType.create(factory), name);
  }
}
