package slimeknights.mantle.data.predicate.damage;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.PredicateRegistry;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;

import java.util.List;
import java.util.function.Predicate;

import static slimeknights.mantle.data.registry.GenericLoaderRegistry.SingletonLoader.singleton;

/**
 * Predicate testing for damage sources
 */
public interface DamageSourcePredicate extends IJsonPredicate<DamageSource> {
  /** Predicate that matches all sources */
  DamageSourcePredicate ANY = simple(source -> true);
  /** Loader for item predicates */
  PredicateRegistry<DamageSource> LOADER = new PredicateRegistry<>("Damage Source Predicate", ANY);

  /* Vanilla getters */
  DamageSourcePredicate PROJECTILE = simple(DamageSource::isProjectile);
  DamageSourcePredicate EXPLOSION = simple(DamageSource::isExplosion);
  DamageSourcePredicate BYPASS_ARMOR = simple(DamageSource::isBypassArmor);
  DamageSourcePredicate DAMAGE_HELMET = simple(DamageSource::isDamageHelmet);
  DamageSourcePredicate BYPASS_INVULNERABLE = simple(DamageSource::isBypassInvul);
  DamageSourcePredicate BYPASS_MAGIC = simple(DamageSource::isBypassMagic);
  DamageSourcePredicate FIRE = simple(DamageSource::isFire);
  DamageSourcePredicate MAGIC = simple(DamageSource::isMagic);
  DamageSourcePredicate FALL = simple(DamageSource::isFall);

  /** Damage that protection works against */
  DamageSourcePredicate CAN_PROTECT = simple(source -> !source.isBypassMagic() && !source.isBypassInvul());
  /** Custom concept: damage dealt by non-projectile entities */
  DamageSourcePredicate MELEE = simple(source -> {
    if (source.isProjectile()) {
      return false;
    }
    // if it's caused by an entity, require it to simply not be thorns
    // meets most normal melee attacks, like zombies, but also means a melee fire or melee magic attack will work
    if (source.getEntity() != null) {
      return source instanceof EntityDamageSource entityDamage && !entityDamage.isThorns();
    } else {
      // for non-entity damage, require it to not be any other type
      // blocks fall damage, falling blocks, cactus, but not starving, drowning, freezing
      return !source.isBypassArmor() && !source.isFire() && !source.isMagic() && !source.isExplosion();
    }
  });

  @Override
  default IJsonPredicate<DamageSource> inverted() {
    return LOADER.invert(this);
  }

  /** Creates a simple predicate with no parameters */
  static DamageSourcePredicate simple(Predicate<DamageSource> predicate) {
    return singleton(loader -> new DamageSourcePredicate() {
      @Override
      public boolean matches(DamageSource source) {
        return predicate.test(source);
      }

      @Override
      public IGenericLoader<? extends DamageSourcePredicate> getLoader() {
        return loader;
      }
    });
  }


  /* Helper methods */

  /** Creates an and predicate */
  @SafeVarargs
  static IJsonPredicate<DamageSource> and(IJsonPredicate<DamageSource>... predicates) {
    return LOADER.and(List.of(predicates));
  }

  /** Creates an or predicate */
  @SafeVarargs
  static IJsonPredicate<DamageSource> or(IJsonPredicate<DamageSource>... predicates) {
    return LOADER.or(List.of(predicates));
  }
}
