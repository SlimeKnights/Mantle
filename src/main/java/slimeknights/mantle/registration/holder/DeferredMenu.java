package slimeknights.mantle.registration.holder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;

/** {@link DeferredHolder} for a {@link MenuType}. Requires using {@link MenuType} for the supplier. */
public class DeferredMenu<T extends AbstractContainerMenu> extends DeferredHolder<MenuType<?>,MenuType<T>> {
  protected DeferredMenu(ResourceKey<MenuType<?>> key) {
    super(key);
  }

  /** Creates a new instance for the given resource location */
  public static <T extends AbstractContainerMenu> DeferredMenu<T> createType(ResourceLocation key) {
    return createType(ResourceKey.create(Registries.MENU, key));
  }

  /** Creates a new instance for the given resource key */
  public static <T extends AbstractContainerMenu> DeferredMenu<T> createType(ResourceKey<MenuType<?>> key) {
    return new DeferredMenu<>(key);
  }
}
