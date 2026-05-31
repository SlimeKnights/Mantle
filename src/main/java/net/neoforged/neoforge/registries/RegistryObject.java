package slimeknights.mantle.compat.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Compatibility shim for code still using the old one-parameter registry object type. */
public class RegistryObject<T> extends DeferredHolder<T,T> {
  public static <T> RegistryObject<T> legacyCreate(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation valueName) {
    return new RegistryObject<>(ResourceKey.create(registryKey, valueName));
  }

  public static <T> RegistryObject<T> legacyCreate(ResourceKey<T> key) {
    return new RegistryObject<>(key);
  }

  public static <T> RegistryObject<T> create(ResourceLocation valueName, Registry<T> registry) {
    return legacyCreate(registry.key(), valueName);
  }

  public static <T> RegistryObject<T> create(ResourceLocation valueName, ResourceKey<? extends Registry<T>> registryKey) {
    return legacyCreate(registryKey, valueName);
  }

  public static <T> RegistryObject<T> create(ResourceLocation valueName, IForgeRegistry<T> registry) {
    return legacyCreate(registry.getRegistryKey(), valueName);
  }

  protected RegistryObject(ResourceKey<T> key) {
    super(key);
  }

  public ResourceLocation getId() {
    return key.location();
  }
}
