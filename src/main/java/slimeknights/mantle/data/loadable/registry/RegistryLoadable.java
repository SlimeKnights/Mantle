package slimeknights.mantle.data.loadable.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.util.RegistryHelper;

import java.util.Objects;

/**
 * Loadable for a registry entry from a  built-in registry.
 * @see LazyRegistryLoadable
 * @see RegistryHolderLoadable
 */
@SuppressWarnings("unused")  // API
public record RegistryLoadable<T>(Registry<T> registry, ResourceLocation registryId) implements BaseRegistryLoadable<T> {
  public RegistryLoadable(ResourceKey<? extends Registry<T>> registryId) {
    this(Objects.requireNonNull(RegistryHelper.getRegistry(registryId), "Unknown registry " + registryId.location()), registryId.location());
  }

  @SuppressWarnings("unchecked")
  public RegistryLoadable(Registry<T> registry) {
    this(registry, Objects.requireNonNull(((Registry<Registry<?>>) BuiltInRegistries.REGISTRY).getKey(registry)));
  }
}
