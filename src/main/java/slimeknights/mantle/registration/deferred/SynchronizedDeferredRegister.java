package slimeknights.mantle.registration.deferred;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/** Deferred register instance that synchronizes register calls */
@RequiredArgsConstructor(staticName = "create")
public class SynchronizedDeferredRegister<T> {
  private final DeferredRegister<T> internal;

  /** Creates a new instance for the given resource key */
  public static <T> SynchronizedDeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String modid) {
    return create(DeferredRegister.create(key, modid));
  }

  /** Creates a new instance for the given forge registry */
  public static <B extends IForgeRegistryEntry<B>> SynchronizedDeferredRegister<B> create(IForgeRegistry<B> registry, String modid) {
    return create(DeferredRegister.create(registry, modid));
  }

  /** Registers the given object, synchronized over the internal register */
  public <I extends T> RegistryObject<I> register(final String name, final Supplier<? extends I> sup) {
    synchronized (internal) {
      return internal.register(name, sup);
    }
  }

  /**
   * Registers the internal register with the event bus
   */
  public void register(IEventBus bus) {
    internal.register(bus);
  }
}
