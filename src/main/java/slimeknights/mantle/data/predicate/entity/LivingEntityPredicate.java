package slimeknights.mantle.data.predicate.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.GenericLoaderRegistry;
import slimeknights.mantle.data.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.data.GenericLoaderRegistry.SingletonLoader;
import slimeknights.mantle.data.predicate.AndJsonPredicate;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.InvertedJsonPredicate;
import slimeknights.mantle.data.predicate.NestedJsonPredicateLoader;
import slimeknights.mantle.data.predicate.OrJsonPredicate;

import java.util.function.Predicate;

/** Predicate matching an entity */
public interface LivingEntityPredicate extends IJsonPredicate<LivingEntity> {
  /** Predicate that matches all entities */
  LivingEntityPredicate ANY = simple(entity -> true);

  /** Loader for block state predicates */
  GenericLoaderRegistry<IJsonPredicate<LivingEntity>> LOADER = new GenericLoaderRegistry<>(ANY, true);
  /** Loader for inverted conditions */
  InvertedJsonPredicate.Loader<LivingEntity> INVERTED = new InvertedJsonPredicate.Loader<>(LOADER);
  /** Loader for and conditions */
  NestedJsonPredicateLoader<LivingEntity,AndJsonPredicate<LivingEntity>> AND = AndJsonPredicate.createLoader(LOADER, INVERTED);
  /** Loader for or conditions */
  NestedJsonPredicateLoader<LivingEntity,OrJsonPredicate<LivingEntity>> OR = OrJsonPredicate.createLoader(LOADER, INVERTED);

  /** Gets an inverted condition */
  @Override
  default IJsonPredicate<LivingEntity> inverted() {
    return INVERTED.create(this);
  }

  /* Singletons */

  /** Predicate that matches water sensitive entities */
  LivingEntityPredicate WATER_SENSITIVE = simple(LivingEntity::isSensitiveToWater);
  /** Predicate that matches fire immune entities */
  LivingEntityPredicate FIRE_IMMUNE = simple(Entity::fireImmune);
  /** Predicate that matches fire immune entities */
  LivingEntityPredicate ON_FIRE = simple(Entity::isOnFire);
  /** Checks if the entity is on the ground */
  LivingEntityPredicate ON_GROUND = simple(Entity::isOnGround);
  /** Entities that are in the air */
  LivingEntityPredicate CROUCHING = simple(Entity::isCrouching);

  // water
  /** Entities with eyes in water */
  LivingEntityPredicate EYES_IN_WATER = simple(entity -> entity.wasEyeInWater);
  /** Entities with feet in water */
  LivingEntityPredicate FEET_IN_WATER = simple(Entity::isInWater);
  /** Entities with head and feet are in water */
  LivingEntityPredicate UNDERWATER = simple(Entity::isUnderWater);
  /** Checks if the entity is being hit by rain at their location */
  LivingEntityPredicate RAINING = simple(entity -> entity.level.isRainingAt(entity.blockPosition()));

  /** Creates a new predicate singleton */
  static LivingEntityPredicate simple(Predicate<LivingEntity> predicate) {
    return SingletonLoader.singleton(loader -> new LivingEntityPredicate() {
      @Override
      public boolean matches(LivingEntity entity) {
        return predicate.test(entity);
      }

      @Override
      public IGenericLoader<? extends IJsonPredicate<LivingEntity>> getLoader() {
        return loader;
      }
    });
  }
}
