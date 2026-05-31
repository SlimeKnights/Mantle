package slimeknights.mantle.compat.neoforged.neoforge.registries;

import java.util.Iterator;
import net.minecraft.core.Registry;

/** Compatibility shim for old Forge registry access. */
public class ForgeRegistry<T> implements IForgeRegistry<T> {
  private final Registry<T> registry;

  public ForgeRegistry(Registry<T> registry) {
    this.registry = registry;
  }

  @Override
  public Registry<T> vanilla() {
    return registry;
  }

  @Override
  public Iterator<T> iterator() {
    return registry.iterator();
  }

  public void unfreeze() {}

  public void register(net.minecraft.resources.ResourceLocation id, T value) {
    Registry.register(registry, id, value);
  }
}
