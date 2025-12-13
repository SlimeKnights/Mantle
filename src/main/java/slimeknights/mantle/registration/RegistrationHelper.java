package slimeknights.mantle.registration;

import com.mojang.brigadier.arguments.ArgumentType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.WoodType;
import slimeknights.mantle.util.RegistryHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegistrationHelper {
  /** Wood types to register with the texture atlas */
  private static final List<WoodType> WOOD_TYPES = new ArrayList<>();

  /** Properties for a standard bucket item */
  public static final Item.Properties BUCKET_PROPS = new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1);

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

  // Note: MissingMappingsEvent (Forge-style registry remaps during load) was removed in NeoForge 1.21.
  // If you were using it for world-upgrade renames, you now need to use Mojang DataFixers or keep the old IDs registered.
  // If you were using it only to refresh caches after numeric ID changes, subscribe to net.neoforged.neoforge.registries.IdMappingEvent instead.

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

  /** Casts the class type to make it a valid argument type */
  @SuppressWarnings("unchecked")
  public static <T extends ArgumentType<?>> Class<T> genericArgumentType(Class<? super T> type) {
    return (Class<T>) type;
  }
}
