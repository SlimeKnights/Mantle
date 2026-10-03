package slimeknights.mantle.data.loadable.registry;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.RegistryHelper;

import javax.annotation.Nullable;

/**
 * Registry loadable for a registry that may not exist immediately upon game launch, such as a Forge registry.
 * Should not be used for world registries; those will need to be handled using the {@link slimeknights.mantle.util.typed.TypedMap} context.
 * @see RegistryLoadable
 */
@RequiredArgsConstructor
public class LazyRegistryLoadable<T> implements BaseRegistryLoadable<T> {
  private final ResourceKey<? extends Registry<T>> registryKey;
  @Nullable
  private Registry<T> registry;

  @Nullable
  @Override
  public Registry<T> registry() {
    if (registry == null) {
      registry = RegistryHelper.getRegistry(registryKey);
      // log if the registry is not found. This may be logged multiple times to prevent accidentally caching a negative
      if (registry == null) {
        // TODO: is including a stack trace (under config) worth it? or would that likely just run too early?
        Mantle.logger.error("Registry {} cannot be located, treating as an empty registry. This may cause serialization issues down the line.", registryKey);
      }
    }
    return registry;
  }

  @Override
  public ResourceLocation registryId() {
    return registryKey.location();
  }
}
