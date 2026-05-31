package slimeknights.mantle.data.predicate.damage;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.TagPredicateRegistry;

import java.util.List;
import java.util.function.Predicate;

import static slimeknights.mantle.data.loadable.record.SingletonLoader.singleton;

/**
 * Predicate testing for damage sources
 */
public interface DamageSourcePredicate extends IJsonPredicate<DamageSource> {
  /** Predicate that matches all sources */
  DamageSourcePredicate ANY = simple(source -> true);
  /** Predicate that matches no sources */
  DamageSourcePredicate NONE = simple(source -> false);
  /** Loader for item predicates */
  TagPredicateRegistry<DamageType, DamageSource> LOADER = new TagPredicateRegistry<>("Damage Source Predicate", ANY, NONE, Loadables.DAMAGE_TYPE_TAG, (tag, source) -> source.is(tag));

  /** Damage that is caused by an entity using another entity */
  DamageSourcePredicate IS_INDIRECT = simple(source -> source.getDirectEntity() != source.getEntity());
  /** Damage that is caused by an entity */
  DamageSourcePredicate HAS_ENTITY = simple(source -> source.getEntity() != null);
  /** Damage that protection works against */
  DamageSourcePredicate CAN_PROTECT = simple(source -> !source.is(DamageTypeTags.BYPASSES_EFFECTS) && !source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY));

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
      public RecordLoadable<? extends DamageSourcePredicate> getLoader() {
        return loader;
      }
    });
  }


  /* Helper methods */

  /** Creates a new predicate for a tag match */
  static IJsonPredicate<DamageSource> tag(TagKey<DamageType> tag) {
    return LOADER.tag(tag);
  }

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
