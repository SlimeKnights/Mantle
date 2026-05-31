package slimeknights.mantle.compat.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

import java.util.List;

/** Legacy compatibility shim for mods still compiling against Forge's missing mappings event. */
public class MissingMappingsEvent extends Event {
  public <T> List<Mapping<T>> getAllMappings(ResourceKey<? extends Registry<T>> registry) {
    return List.of();
  }

  public static class Mapping<T> {
    private final ResourceLocation key;

    public Mapping(ResourceLocation key) {
      this.key = key;
    }

    public ResourceLocation getKey() {
      return key;
    }

    public void remap(T value) {}
  }
}
