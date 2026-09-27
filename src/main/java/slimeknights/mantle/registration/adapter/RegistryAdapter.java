package slimeknights.mantle.registration.adapter;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoadingContext;

import javax.annotation.CheckForNull;
import java.util.Objects;

/**
 * A convenience wrapper for forge registries, to be used in combination with {@link net.neoforged.neoforge.registries.RegisterEvent}.
 * Simply put it allows you to register things by passing (thing, name) instead of having to set the name inline.
 * There also is a convenience variant for items and itemblocks, see {@link ItemRegistryAdapter}.
 */
@SuppressWarnings("WeakerAccess")
public class RegistryAdapter<T> {
  private final Registry<T> registry;
  private final String modId;

  public RegistryAdapter(@CheckForNull Registry<T> registry, String modId) {
    this.registry = Objects.requireNonNull(registry);
    this.modId = modId;
  }

  /**
   * Automatically creates determines the modid from the currently loading mod.
   * If this results in the wrong namespace, use the other constructor where you can provide the modid.
   * The modid is used as the namespace for resource locations, so if your mods id is "foo" it will register an item "bar" as "foo:bar".
   */
  public RegistryAdapter(@CheckForNull Registry<T> registry) {
    this(Objects.requireNonNull(registry), ModLoadingContext.get().getActiveContainer().getModId());
  }

  /**
   * Construct a resource location that belongs to the given namespace. Usually your mod.
   * @param name  Name for location
   */
  public ResourceLocation getResource(String name) {
    return ResourceLocation.fromNamespaceAndPath(modId, name);
  }

  /**
   * Construct a resource location string that belongs to the given namespace. Usually your mod.
   * @param name  Name for location
   */
  public String resourceName(String name) {
    return modId + ":" + name;
  }

  /**
   * General purpose registration method. Just pass the name you want your thing registered as.
   * @param entry  Entry to register
   * @param name   Registry name
   * @return Registry entry
   */
  public <I extends T> I register(I entry, String name) {
    return this.register(entry, this.getResource(name));
  }

  /**
   * Registers an entry using the name from another entry
   * @param entry  Entry to register
   * @param name   Entry name to copy
   * @param <I>    Value type
   * @return  Registered entry
   */
  public <I extends T> I register(I entry, T name) {
    return this.register(entry, Objects.requireNonNull(registry.getKey(name)));
  }

  /**
   * General purpose backup registration method. In case you want to set a very specific resource location.
   * You should probably use the special purpose methods instead of this.
   * <p>
   * Note: changes the things registry name. Do not call this with already registered objects!
   * @param entry     Entry to register
   * @param location  Registry name
   * @return Registry entry
   */
  public <I extends T> I register(I entry, ResourceLocation location) {
    Registry.register(registry, location, entry);
    return entry;
  }
}
