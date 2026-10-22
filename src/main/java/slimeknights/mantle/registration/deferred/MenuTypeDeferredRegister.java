package slimeknights.mantle.registration.deferred;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeknights.mantle.registration.holder.DeferredMenu;

import java.util.function.Function;
import java.util.function.Supplier;

/** {@link DeferredRegister} for {@link MenuType}, Automatically maps a factory argument in {@link IMenuTypeExtension}. */
@SuppressWarnings("unused")
public class MenuTypeDeferredRegister extends DeferredRegister<MenuType<?>> {
  public MenuTypeDeferredRegister(String modID) {
    super(Registries.MENU, modID);
  }

  @SuppressWarnings({"RedundantCast", "rawtypes", "unchecked"})
  @Override
  protected <I extends MenuType<?>> DeferredHolder<MenuType<?>, I> createHolder(ResourceKey<? extends Registry<MenuType<?>>> registryKey, ResourceLocation key) {
    // this should be fine as long as no one uses the raw register and extends menu type
    return (DeferredHolder<MenuType<?>, I>)(DeferredHolder) DeferredMenu.createType(ResourceKey.create(registryKey, key));
  }

  /** @deprecated use {@link #register(String, IContainerFactory)} */
  @Deprecated
  @Override
  public <I extends MenuType<?>> DeferredHolder<MenuType<?>, I> register(String name, Function<ResourceLocation, ? extends I> func) {
    return super.register(name, func);
  }

  /** @deprecated use {@link #register(String, IContainerFactory)} */
  @Deprecated
  @Override
  public <I extends MenuType<?>> DeferredHolder<MenuType<?>, I> register(String name, Supplier<? extends I> sup) {
    return super.register(name, sup);
  }

  /**
   * Registers a container type
   * @param name     Container name
   * @param factory  Container factory
   * @param <C>      Container type
   * @return  Registry object containing the container type
   */
  public <C extends AbstractContainerMenu> DeferredMenu<C> register(String name, IContainerFactory<C> factory) {
    return (DeferredMenu<C>) register(name, () -> IMenuTypeExtension.create(factory));
  }
}
