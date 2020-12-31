package slimeknights.mantle.registration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraftforge.event.RegistryEvent.MissingMappings;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.IRegistryDelegate;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegistrationHelper {
  /**
   * Used to mark injected registry objects, as despite being set to null they will be nonnull at runtime.
   * @param <T>  Class type
   * @return  Null, its a lie
   */
  @SuppressWarnings("ConstantConditions")
  public static <T> T injected() {
    return null;
  }

  /**
   * Creates a supplier for a specific registry entry instance based on the delegate to a general instance.
   * Note that this performs an unchecked cast, be certain that the right type is returned
   * @param delegate  Delegate instance
   * @param <I>  Forge registry type
   * @return  Supplier for the given instance
   */
  @SuppressWarnings("unchecked")
  public static <I extends IForgeRegistryEntry<? super I>> Supplier<I> castDelegate(IRegistryDelegate<? super I> delegate) {
    return () -> (I) delegate.get();
  }

  /**
   * Handles missing mappings for the given registry
   * @param event    Mappings event
   * @param handler  Mapping handler
   * @param <T>      Event type
   */
  public static <T extends IForgeRegistryEntry<T>> void handleMissingMappings(MissingMappings<T> event, String modID, Function<String, T> handler) {
    for (Mapping<T> mapping : event.getAllMappings()) {
      if (modID.equals(mapping.key.getNamespace())) {
        @Nullable T value = handler.apply(mapping.key.getPath());
        if (value != null) {
          mapping.remap(value);
        }
      }
    }
  }
}
