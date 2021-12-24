package slimeknights.mantle.registration.deferred;

import net.minecraft.util.StringRepresentable;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import slimeknights.mantle.registration.object.EnumObject;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Base logic for a deferred register wrapper
 * @param <T>  Registry type
 */
@SuppressWarnings("WeakerAccess")
public abstract class DeferredRegisterWrapper<T extends IForgeRegistryEntry<T>> {
  /** Registry instance, use this to provide register methods */
  protected final DeferredRegister<T> register;
  /** Mod ID for registration */
  private final String modID;

  protected DeferredRegisterWrapper(IForgeRegistry<T> reg, String modID) {
    register = DeferredRegister.create(reg, modID);
    this.modID = modID;
  }

  /**
   * Initializes this registry wrapper. Needs to be called during mod construction
   */
  public void register(IEventBus bus) {
    register.register(bus);
  }

  /* Utilities */

  /**
   * Gets a resource location object for the given name
   * @param name  Name
   * @return  Resource location string
   */
  protected ResourceLocation resource(String name) {
    return new ResourceLocation(modID, name);
  }

  /**
   * Gets a resource location string for the given name
   * @param name  Name
   * @return  Resource location string
   */
  protected String resourceName(String name) {
    return modID + ":" + name;
  }


  /* Enum objects */

  /**
   * Registers an item with multiple variants, prefixing the name with the value name
   * @param values    Enum values to use for this block
   * @param name      Name of the block
   * @param register  Function to register an entry
   * @return  EnumObject mapping between different block types
   */
  protected static <E extends Enum<E> & StringRepresentable, V extends T, T extends IForgeRegistryEntry<T>> EnumObject<E,V> registerEnum(E[] values, String name, BiFunction<String,E,Supplier<? extends V>> register) {
    if (values.length == 0) {
      throw new IllegalArgumentException("Must have at least one value");
    }
    // note this cast only works because you cannot extend an enum
    EnumObject.Builder<E,V> builder = new EnumObject.Builder<>(values[0].getDeclaringClass());
    for (E value : values) {
      builder.put(value, register.apply(value.getSerializedName() + "_" + name, value));
    }
    return builder.build();
  }

  /**
   * Registers an item with multiple variants, suffixing the name with the value name
   * @param name      Name of the block
   * @param values    Enum values to use for this block
   * @param register  Function to register an entry
   * @return  EnumObject mapping between different block types
   */
  protected static <E extends Enum<E> & StringRepresentable, V extends T, T extends IForgeRegistryEntry<T>> EnumObject<E,V> registerEnum(String name, E[] values, BiFunction<String,E,Supplier<? extends V>> register) {
    if (values.length == 0) {
      throw new IllegalArgumentException("Must have at least one value");
    }
    // note this cast only works because you cannot extend an enum
    EnumObject.Builder<E,V> builder = new EnumObject.Builder<>(values[0].getDeclaringClass());
    for (E value : values) {
      builder.put(value, register.apply(name + "_" + value.getSerializedName(), value));
    }
    return builder.build();
  }
}
