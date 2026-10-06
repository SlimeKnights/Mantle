package slimeknights.mantle.registration.deferred;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeknights.mantle.registration.object.EnumObject;

import java.util.Locale;
import java.util.function.Function;

/** Generic deferred register for an object using registry objects and wanting enums */
@SuppressWarnings("unused")  // API
public class EnumDeferredRegister<R> extends DeferredRegister<R> {
  public EnumDeferredRegister(ResourceKey<Registry<R>> reg, String modID) {
    super(reg, modID);
  }

  /**
   * Gets a resource location object for the given name
   * @param name  Name
   * @return  Resource location string
   */
  protected ResourceLocation resource(String name) {
    return ResourceLocation.fromNamespaceAndPath(getNamespace(), name);
  }

  /**
   * Gets a resource location string for the given name
   * @param name  Name
   * @return  Resource location string
   */
  protected String resourceName(String name) {
    return getNamespace() + ":" + name;
  }

  /**
   * Registers an object with multiple variants, using the given name mapper.
   * @param values      Enum values to use for this item
   * @param nameGetter  Function to get the name from each enum element
   * @param mapper      Function to get an object for the given enum value
   * @return  EnumObject mapping between different item types
   */
  public <E extends Enum<E>> EnumObject<E,R> registerEnum(E[] values, Function<? super E,String> nameGetter, Function<E,? extends R> mapper) {
    return EnumObject.generate(values, value -> register(nameGetter.apply(value), () -> mapper.apply(value)));
  }

  /**
   * Registers an object with multiple variants, prefixing the name with the value name
   * @param values   Enum values to use for this item
   * @param name     Name of the object
   * @param mapper   Function to get an object for the given enum value
   * @return  EnumObject mapping between different item types
   */
  public <E extends Enum<E>> EnumObject<E,R> registerEnum(E[] values, String name, Function<E,? extends R> mapper) {
    return registerEnum(values, suffix(name), mapper);
  }

  /**
   * Registers an object with multiple variants, suffixing the name with the value name
   * @param values   Enum values to use for this item
   * @param name     Name of the object
   * @param mapper   Function to get an object for the given enum value
   * @return  EnumObject mapping between different item types
   */
  public <E extends Enum<E>> EnumObject<E,R> registerEnum(String name, E[] values, Function<E,? extends R> mapper) {
    return registerEnum(values, prefix(name), mapper);
  }


  /* Static helpers */

  /** Gets the name of an enum value */
  private static String getName(Enum<?> value) {
    return value instanceof StringRepresentable representable ? representable.getSerializedName() : value.name().toLowerCase(Locale.ROOT);
  }

  /** Creates a name function for prefixing the enum name. */
  public static Function<Enum<?>,String> prefix(String prefix) {
    return e -> prefix + '_' + getName(e);
  }

  /** Creates a name function for suffixing the enum name. */
  public static Function<Enum<?>,String> suffix(String suffix) {
    return e -> getName(e) + '_' + suffix;
  }
}
