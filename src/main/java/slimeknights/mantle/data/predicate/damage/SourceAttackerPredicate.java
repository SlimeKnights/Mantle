package slimeknights.mantle.data.predicate.damage;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.entity.LivingEntityPredicate;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;

/**
 * Predicate that checks for properties of the attacker in a damage source
 */
public record SourceAttackerPredicate(IJsonPredicate<LivingEntity> attacker) implements DamageSourcePredicate {
  public static final RecordLoadable<SourceAttackerPredicate> LOADER = RecordLoadable.create(LivingEntityPredicate.LOADER.directField("entity_type", SourceAttackerPredicate::attacker), SourceAttackerPredicate::new);

  @Override
  public boolean matches(DamageSource source) {
    return source.getEntity() instanceof LivingEntity living && attacker.matches(living);
  }

  @Override
  public IGenericLoader<? extends DamageSourcePredicate> getLoader() {
    return LOADER;
  }
}
