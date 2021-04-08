package slimeknights.mantle.registration.deferred;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registry;

import java.util.function.Supplier;

/**
 * Deferred register for an entity, building the type from a builder instance and adding an egg
 */
@SuppressWarnings("unused")
public class EntityTypeDeferredRegister extends DeferredRegisterWrapper {

  public EntityTypeDeferredRegister(String modID) {
    super(modID);
  }

  /**
   * Registers a entity type for the given entity type builder
   * @param name  Entity name
   * @param sup   Entity builder instance
   * @param <T>   Entity class type
   * @return  Entity registry object
   */
  public <T extends Entity> EntityType<T> register(String name, Supplier<EntityType.Builder<T>> sup) {
    return Registry.register(Registry.ENTITY_TYPE, name, sup);
  }

  /**
   * Registers a entity type for the given entity type builder, and registers a spawn egg for it
   * @param name       Entity name
   * @param sup        Entity builder instance
   * @param primary    Primary egg color
   * @param secondary  Secondary egg color
   * @param <T>   Entity class type
   * @return  Entity registry object
   */
  public <T extends Entity> RegistryObject<EntityType<T>> registerWithEgg(String name, Supplier<EntityType.Builder<T>> sup, int primary, int secondary) {
    Lazy<EntityType<T>> lazy = Lazy.of(() -> sup.get().build(resourceName(name)));
//    itemRegistry.register(name + "_spawn_egg", () -> new SpawnEggItem(lazy.get(), primary, secondary, ItemProperties.EGG_PROPS));
    return register.register(name, lazy);
  }
}
