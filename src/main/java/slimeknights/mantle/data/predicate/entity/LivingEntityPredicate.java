package slimeknights.mantle.data.predicate.entity;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.loadable.record.SingletonLoader;
import slimeknights.mantle.data.predicate.FallbackPredicateRegistry;
import slimeknights.mantle.data.predicate.IJsonPredicate;

import java.util.List;
import java.util.function.Predicate;

/**
 * Predicate matching a {@link LivingEntity}.
 * @see EntityPredicate
 */
public interface LivingEntityPredicate extends IJsonPredicate<LivingEntity> {
  /** Predicate that matches all entities */
  LivingEntityPredicate ANY = simple(entity -> true);
  /** Predicate that matches all entities */
  LivingEntityPredicate NONE = simple(entity -> false);
  /** Loader for block state predicates */
  FallbackPredicateRegistry<LivingEntity,Entity> LOADER = new FallbackPredicateRegistry<>("Living Entity Predicate", ANY, NONE, EntityPredicate.LOADER, entity -> entity, "entity");

  /** Gets an inverted condition */
  @Override
  default IJsonPredicate<LivingEntity> inverted() {
    return LOADER.invert(this);
  }


  /* Singletons */

  /** Predicate that matches water sensitive entities */
  LivingEntityPredicate WATER_SENSITIVE = simple(LivingEntity::isSensitiveToWater);
  /** Entities blocking with a valid shield */
  LivingEntityPredicate BLOCKING = simple(LivingEntity::isBlocking);
  /** Entities actively flying with an elytra */
  LivingEntityPredicate ELYTRA_FLYING = simple(LivingEntity::isFallFlying);


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

  /** Creates an item predicate */
  static IJsonPredicate<LivingEntity> fallback(IJsonPredicate<Entity> predicate) {
    return LOADER.fallback(predicate);
  }

  /** Creates an entity predicate using the given living entity predicate. Will match if the entity type is living and the nested predicate matches. */
  static IJsonPredicate<Entity> asEntity(IJsonPredicate<LivingEntity> predicate) {
    return new LivingEntityEntityPredicate(predicate);
  }

  /** Creates an entity set predicate */
  static IJsonPredicate<LivingEntity> set(EntityType<?>... types) {
    return fallback(EntityPredicate.set(types));
  }

  /** Creates a tag predicate */
  static IJsonPredicate<LivingEntity> tag(TagKey<EntityType<?>> tag) {
    return fallback(EntityPredicate.tag(tag));
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
