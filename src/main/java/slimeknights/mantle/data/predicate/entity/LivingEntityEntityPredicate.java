package slimeknights.mantle.data.predicate.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;

/**
 * {@link EntityPredicate} matching a {@link LivingEntity} through {@link LivingEntityPredicate}
 * @apiNote use {@link LivingEntityPredicate#asEntity(IJsonPredicate)}
 */
@Internal
public record LivingEntityEntityPredicate(IJsonPredicate<LivingEntity> predicate) implements EntityPredicate {
  public static final RecordLoadable<LivingEntityEntityPredicate> LOADER = RecordLoadable.create(LivingEntityPredicate.LOADER.directField("living_type", LivingEntityEntityPredicate::predicate), LivingEntityEntityPredicate::new);

  @Override
  public RecordLoadable<LivingEntityEntityPredicate> getLoader() {
    return LOADER;
  }

  @Override
  public boolean matches(Entity entity) {
    return entity instanceof LivingEntity living && predicate.matches(living);
  }
}
