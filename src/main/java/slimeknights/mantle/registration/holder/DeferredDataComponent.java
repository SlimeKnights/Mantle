package slimeknights.mantle.registration.holder;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

/** {@link DeferredHolder} for {@link DataComponentType}. Requires using {@link DataComponentType} for the supplier. */
public class DeferredDataComponent<T> extends DeferredHolder<DataComponentType<?>, DataComponentType<T>> {
  protected DeferredDataComponent(ResourceKey<DataComponentType<?>> key) {
    super(key);
  }

  /** Creates a new instance for the given resource location */
  public static <T> DeferredDataComponent<T> createType(ResourceLocation key) {
    return createType(ResourceKey.create(Registries.DATA_COMPONENT_TYPE, key));
  }

  /** Creates a new instance for the given resource key */
  public static <T> DeferredDataComponent<T> createType(ResourceKey<DataComponentType<?>> key) {
    return new DeferredDataComponent<>(key);
  }
}
