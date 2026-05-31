package slimeknights.mantle.data.predicate.entity;

import net.minecraft.tags.TagKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.loadable.record.SingletonLoader;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.RegistryPredicateRegistry;

import java.util.List;
import java.util.function.Predicate;

/**
 * Predicate matching an entity
 * TODO 1.21: separate out into {@code LivingEntityPredicate} and {@code EntityPredicate}
 */
public interface LivingEntityPredicate extends IJsonPredicate<LivingEntity> {
  /** Predicate that matches all entities */
  LivingEntityPredicate ANY = simple(entity -> true);
  /** Predicate that matches all entities */
  LivingEntityPredicate NONE = simple(entity -> false);
  /** Loader for block state predicates */
  RegistryPredicateRegistry<EntityType<?>,LivingEntity> LOADER = new RegistryPredicateRegistry<>("Entity Predicate", ANY, NONE, Loadables.ENTITY_TYPE, Entity::getType, "entities", Loadables.ENTITY_TYPE_TAG, (tag, entity) -> entity.getType().is(tag));

  /** Gets an inverted condition */
  @Override
  default IJsonPredicate<LivingEntity> inverted() {
    return LOADER.invert(this);
  }


  /* Singletons */

  /** Predicate that matches water sensitive entities */
  LivingEntityPredicate WATER_SENSITIVE = simple(LivingEntity::isSensitiveToWater);
  /** Predicate that matches fire immune entities */
  LivingEntityPredicate FIRE_IMMUNE = simple(Entity::fireImmune);
  /** Predicate that matches fire immune entities */
  LivingEntityPredicate ON_FIRE = simple(Entity::isOnFire);
  /** Predicate that matches entities that can freeze */
  LivingEntityPredicate CAN_FREEZE = simple(Entity::canFreeze);
  /** Predicate that matches entities that are freezing */
  LivingEntityPredicate IS_FREEZING = simple(entity -> entity.getTicksFrozen() >= entity.getTicksRequiredToFreeze());
  /** Predicate that matches entities that are freezing */
  LivingEntityPredicate IS_IN_POWDERED_SNOW = simple(Entity::isFreezing);
  /** Checks if the entity is on the ground */
  LivingEntityPredicate ON_GROUND = simple(Entity::onGround);
  /** Entities that are in the air */
  LivingEntityPredicate CROUCHING = simple(Entity::isCrouching);
  /** Entities that are currently sprinting */
  LivingEntityPredicate SPRINTING = simple(Entity::isSprinting);
  /** Entities blocking with a valid shield */
  LivingEntityPredicate BLOCKING = simple(LivingEntity::isBlocking);
  /** Entities actively flying with an elytra */
  LivingEntityPredicate ELYTRA_FLYING = simple(LivingEntity::isFallFlying);


  // water
  /** Entities with eyes in water */
  LivingEntityPredicate EYES_IN_WATER = simple(entity -> entity.isEyeInFluid(FluidTags.WATER));
  /** Entities with feet in water */
  LivingEntityPredicate FEET_IN_WATER = simple(Entity::isInWater);
  /** Entities with head and feet are in water */
  LivingEntityPredicate UNDERWATER = simple(Entity::isUnderWater);
  /** Checks if the entity is being hit by rain at their location */
  LivingEntityPredicate RAINING = simple(entity -> entity.level().isRainingAt(entity.blockPosition()));

  /** Creates a new predicate singleton */
  static LivingEntityPredicate simple(Predicate<LivingEntity> predicate) {
    return SingletonLoader.singleton(loader -> new LivingEntityPredicate() {
      @Override
      public boolean matches(LivingEntity entity) {
        return predicate.test(entity);
      }

      @Override
      public RecordLoadable<? extends LivingEntityPredicate> getLoader() {
        return loader;
      }
    });
  }


  /* Helper methods */

  /** Creates an entity set predicate */
  static IJsonPredicate<LivingEntity> set(EntityType<?>... types) {
    return LOADER.setOf(types);
  }

  /** Creates a tag predicate */
  static IJsonPredicate<LivingEntity> tag(TagKey<EntityType<?>> tag) {
    return LOADER.tag(tag);
  }

  /** Creates an and predicate */
  @SafeVarargs
  static IJsonPredicate<LivingEntity> and(IJsonPredicate<LivingEntity>... predicates) {
    return LOADER.and(List.of(predicates));
  }

  /** Creates an or predicate */
  @SafeVarargs
  static IJsonPredicate<LivingEntity> or(IJsonPredicate<LivingEntity>... predicates) {
    return LOADER.or(List.of(predicates));
  }
}
