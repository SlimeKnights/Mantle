package slimeknights.mantle.registration.adapter;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.IForgeRegistry;

@SuppressWarnings("unused")
public class ContainerTypeRegistryAdapter extends RegistryAdapter<ScreenHandlerType<?>> {
  /** @inheritDoc */
  public ContainerTypeRegistryAdapter(IForgeRegistry<ScreenHandlerType<?>> registry, String modId) {
    super(registry, modId);
  }

  /** @inheritDoc */
  public ContainerTypeRegistryAdapter(IForgeRegistry<ScreenHandlerType<?>> registry) {
    super(registry);
  }

  /**
   * Registers a container type
   * @param name     Container name
   * @param factory  Container factory
   * @param <C>      Container type
   * @return  Registry object containing the container type
   */
  public <C extends ScreenHandler> ScreenHandlerType<C> registerType(IContainerFactory<C> factory, String name) {
    return register(IForgeContainerType.create(factory), name);
  }
}
