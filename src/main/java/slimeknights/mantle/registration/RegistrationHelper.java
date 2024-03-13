package slimeknights.mantle.registration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.minecraftforge.registries.MissingMappingsEvent.Mapping;
import slimeknights.mantle.util.RegistryHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegistrationHelper {
  /** Wood types to register with the texture atlas */
  private static final List<WoodType> WOOD_TYPES = new ArrayList<>();

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
   * Gets a holder for a registry object
   * @param registry  Registry instance
   * @param entry     Entry to fetch holder
   * @param <T>       Registry type
   * @param <R>       Return type, typically but not strictly registry type
   * @return  Supplier for the given registry casted to the requested type
   */
  @SuppressWarnings("unchecked")  // we know the entry is the given type
  public static <T, R extends T> Supplier<R> getCastedHolder(DefaultedRegistry<T> registry, T entry) {
    Supplier<T> holder = RegistryHelper.getHolder(registry, entry);
    return () -> (R) holder.get();
  }

  /**
   * Handles missing mappings for the given registry
   * @param event    Mappings event
   * @param handler  Mapping handler
   * @param <T>      Event type
   */
  public static <T> void handleMissingMappings(MissingMappingsEvent event, String modID, ResourceKey<? extends Registry<T>> registry, Function<String, T> handler) {
    // event is kinda nice, automatically filters mappings to the registry type via the key
    for (Mapping<T> mapping : event.getAllMappings(registry)) {
      ResourceLocation id = mapping.getKey();
      if (modID.equals(id.getNamespace())) {
        @Nullable T value = handler.apply(id.getPath());
        if (value != null) {
          mapping.remap(value);
        }
      }
    }
  }

  /** Registers a wood type to be injected into the atlas, should be called before client setup */
  public static void registerWoodType(WoodType type) {
    synchronized (WOOD_TYPES) {
      WOOD_TYPES.add(type);
      WoodType.register(type);
    }
  }

  /** Runs the given consumer for each wood type registered */
  public static void forEachWoodType(Consumer<WoodType> consumer) {
    WOOD_TYPES.forEach(consumer);
  }

}
