package slimeknights.mantle.data.predicate.entity;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.loadable.record.SingletonLoader;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.RegistryPredicateRegistry;

import java.util.List;
import java.util.function.Predicate;

/**
 * Predicate matching an {@link Entity}.
 * @see LivingEntityPredicate
 */
public interface EntityPredicate extends IJsonPredicate<Entity> {
  /** Predicate that matches all entities */
  EntityPredicate ANY = simple(entity -> true);
  /** Predicate that matches all entities */
  EntityPredicate NONE = simple(entity -> false);
  /** Loader for block state predicates */
  RegistryPredicateRegistry<EntityType<?>,Entity> LOADER = new RegistryPredicateRegistry<>("Entity Predicate", ANY, NONE, Loadables.ENTITY_TYPE, Entity::getType, "entities", Loadables.ENTITY_TYPE_TAG, (tag, entity) -> entity.getType().is(tag));

  /** Gets an inverted condition */
  @Override
  default IJsonPredicate<Entity> inverted() {
    return LOADER.invert(this);
  }


  /* Singletons */

  /** Predicate that matches fire immune entities */
  EntityPredicate FIRE_IMMUNE = simple(Entity::fireImmune);
  /** Predicate that matches fire immune entities */
  EntityPredicate ON_FIRE = simple(Entity::isOnFire);
  /** Predicate that matches entities that can freeze */
  EntityPredicate CAN_FREEZE = simple(Entity::canFreeze);
  /** Predicate that matches entities that are freezing */
  EntityPredicate IS_FREEZING = simple(entity -> entity.getTicksFrozen() >= entity.getTicksRequiredToFreeze());
  /** Predicate that matches entities that are freezing */
  EntityPredicate IS_IN_POWDERED_SNOW = simple(Entity::isFreezing);
  /** Checks if the entity is on the ground */
  EntityPredicate ON_GROUND = simple(Entity::onGround);
  /** Entities that are in the air */
  EntityPredicate CROUCHING = simple(Entity::isCrouching);
  /** Entities that are currently sprinting */
  EntityPredicate SPRINTING = simple(Entity::isSprinting);

  // water
  /** Entities with eyes in water */
  EntityPredicate EYES_IN_WATER = simple(entity -> entity.wasEyeInWater);
  /** Entities with feet in water */
  EntityPredicate FEET_IN_WATER = simple(Entity::isInWater);
  /** Entities with head and feet are in water */
  EntityPredicate UNDERWATER = simple(Entity::isUnderWater);
  /** Checks if the entity is being hit by rain at their location */
  EntityPredicate RAINING = simple(entity -> entity.level().isRainingAt(entity.blockPosition()));


  /** Creates a new predicate singleton */
  static EntityPredicate simple(Predicate<Entity> predicate) {
    return SingletonLoader.singleton(loader -> new EntityPredicate() {
      @Override
      public boolean matches(Entity entity) {
        return predicate.test(entity);
      }

      @Override
      public RecordLoadable<? extends EntityPredicate> getLoader() {
        return loader;
      }
    });
  }


  /* Helper methods */

  /** Creates an entity set predicate */
  static IJsonPredicate<Entity> set(EntityType<?>... types) {
    return LOADER.setOf(types);
  }

  /** Creates a tag predicate */
  static IJsonPredicate<Entity> tag(TagKey<EntityType<?>> tag) {
    return LOADER.tag(tag);
  }

  /** Creates an and predicate */
  @SafeVarargs
  static IJsonPredicate<Entity> and(IJsonPredicate<Entity>... predicates) {
    return LOADER.and(List.of(predicates));
  }

  /** Creates an or predicate */
  @SafeVarargs
  static IJsonPredicate<Entity> or(IJsonPredicate<Entity>... predicates) {
    return LOADER.or(List.of(predicates));
  }
}
