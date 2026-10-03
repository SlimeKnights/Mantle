package slimeknights.mantle.data.predicate.damage;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.primitive.EnumLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.entity.LivingEntityPredicate;

import javax.annotation.Nullable;

/**
 * Predicate that checks for properties of the attacker in a damage source
 */
@SuppressWarnings("unused")  // API
public record SourceAttackerPredicate(IJsonPredicate<LivingEntity> attacker, WhichEntity which) implements DamageSourcePredicate {
  public static final RecordLoadable<SourceAttackerPredicate> LOADER = RecordLoadable.create(
    LivingEntityPredicate.LOADER.directField("entity_type", SourceAttackerPredicate::attacker),
    new EnumLoadable<>(WhichEntity.class).defaultField("which", WhichEntity.CAUSING, true, SourceAttackerPredicate::which),
    SourceAttackerPredicate::new);

  public static SourceAttackerPredicate causing(IJsonPredicate<LivingEntity> attacker) {
    return new SourceAttackerPredicate(attacker, WhichEntity.CAUSING);
  }

  public static SourceAttackerPredicate direct(IJsonPredicate<LivingEntity> attacker) {
    return new SourceAttackerPredicate(attacker, WhichEntity.DIRECT);
  }

  @Override
  public boolean matches(DamageSource source) {
    Entity entity = which.get(source);
    if (entity instanceof LivingEntity living) {
      return attacker.matches(living);
    }
    return attacker == LivingEntityPredicate.ANY && entity != null;
  }

  @Override
  public RecordLoadable<SourceAttackerPredicate> getLoader() {
    return LOADER;
  }

  /** Which entity is being considered */
  enum WhichEntity {
    CAUSING {
      @Nullable
      @Override
      public Entity get(DamageSource source) {
        return source.getEntity();
      }
    },
    DIRECT {
      @Nullable
      @Override
      public Entity get(DamageSource source) {
        return source.getDirectEntity();
      }
    };

    @Nullable
    public abstract Entity get(DamageSource source);
  }
}
