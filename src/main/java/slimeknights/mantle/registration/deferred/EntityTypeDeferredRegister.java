package slimeknights.mantle.registration.deferred;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeknights.mantle.registration.holder.DeferredEntity;
import slimeknights.mantle.registration.object.EntityObject;

import java.util.function.Function;
import java.util.function.Supplier;

/** {@link DeferredRegister} for an {@link EntityType}. Has helpers for creating eggs and for simplifying builder calls. */
@SuppressWarnings("unused")
public class EntityTypeDeferredRegister extends DeferredRegister<EntityType<?>> {
  private final DeferredRegister<Item> itemRegistry;

  public EntityTypeDeferredRegister(String modID, DeferredRegister<Item> itemRegistry) {
    super(Registries.ENTITY_TYPE, modID);
    this.itemRegistry = itemRegistry;
  }

  public EntityTypeDeferredRegister(String modID) {
    this(modID, DeferredRegister.create(Registries.ITEM, modID));
  }

  @Override
  public void register(IEventBus bus) {
    super.register(bus);
    itemRegistry.register(bus);
  }

  @SuppressWarnings({"unchecked", "RedundantCast", "rawtypes"})
  @Override
  protected <I extends EntityType<?>> DeferredHolder<EntityType<?>, I> createHolder(ResourceKey<? extends Registry<EntityType<?>>> registryKey, ResourceLocation key) {
    // this should be fine as long as no one uses the raw register and extends entity type
    return (DeferredHolder<EntityType<?>, I>) (DeferredHolder) DeferredEntity.createType(ResourceKey.create(registryKey, key));
  }

  /** @deprecated use {@link #registerWithEgg(String, Supplier, int, int)} or {@link #registerNoEgg(String, Supplier)} */
  @Deprecated
  @Override
  public <I extends EntityType<?>> DeferredHolder<EntityType<?>, I> register(String name, Supplier<? extends I> sup) {
    return super.register(name, sup);
  }

  /** @deprecated use {@link #registerWithEgg(String, Supplier, int, int)} or {@link #registerNoEgg(String, Supplier)} */
  @Deprecated
  @Override
  public <I extends EntityType<?>> DeferredHolder<EntityType<?>, I> register(String name, Function<ResourceLocation, ? extends I> func) {
    return super.register(name, func);
  }

  /**
   * Registers an entity type for the given entity type builder with no spawn egg.
   * @param name  Entity name
   * @param sup   Entity builder instance
   * @param <T>   Entity class type
   * @return  Entity registry object
   */
  public <T extends Entity> DeferredEntity<T> registerNoEgg(String name, Supplier<EntityType.Builder<T>> sup) {
    return (DeferredEntity<T>) register(name, id -> sup.get().build(id.toString()));
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
  public <T extends Mob> EntityObject<T> registerWithEgg(String name, Supplier<EntityType.Builder<T>> sup, int primary, int secondary) {
    DeferredEntity<T> object = registerNoEgg(name, sup);
    return new EntityObject<>(object, itemRegistry.register(name + "_spawn_egg", () -> new DeferredSpawnEggItem(object, primary, secondary, new Item.Properties())));
  }
}
