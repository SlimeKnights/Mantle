package slimeknights.mantle.registration.deferred;

import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Deferred register for container types, automatically mapping a factory argument in {@link IForgeContainerType}
 */
@SuppressWarnings("unused")
public class ContainerTypeDeferredRegister extends DeferredRegisterWrapper<ContainerType<?>> {

  public ContainerTypeDeferredRegister(String modID) {
    super(ForgeRegistries.CONTAINERS, modID);
  }

  /**
   * Registers a container type
   * @param name     Container name
   * @param factory  Container factory
   * @param <C>      Container type
   * @return  Registry object containing the container type
   */
  public <C extends Container> RegistryObject<ContainerType<C>> register(String name, IContainerFactory<C> factory) {
    return register.register(name, () -> IForgeContainerType.create(factory));
  }
}
