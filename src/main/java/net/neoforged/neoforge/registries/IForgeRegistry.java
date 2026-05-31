package slimeknights.mantle.compat.neoforged.neoforge.registries;

import java.util.Collection;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/** Compatibility shim for old Forge registry access. */
public interface IForgeRegistry<T> extends Iterable<T> {
  Registry<T> vanilla();

  default boolean containsKey(ResourceLocation id) {
    return vanilla().containsKey(id);
  }

  default T getValue(ResourceLocation id) {
    return vanilla().get(id);
  }

  default ResourceLocation getKey(T value) {
    return vanilla().getKey(value);
  }

  default Collection<T> getValues() {
    return vanilla().stream().toList();
  }

  default Collection<Map.Entry<ResourceKey<T>, T>> getEntries() {
    return vanilla().entrySet();
  }

  default ResourceKey<? extends Registry<T>> getRegistryKey() {
    return vanilla().key();
  }
}
