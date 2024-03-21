package slimeknights.mantle.data.predicate.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;

import java.util.Set;

/** Predicate matching entities from a set */
public record EntitySetPredicate(Set<EntityType<?>> entities) implements LivingEntityPredicate {
  public static final RecordLoadable<EntitySetPredicate> LOADER = RecordLoadable.create(Loadables.ENTITY_TYPE.set().field("entities", EntitySetPredicate::entities), EntitySetPredicate::new);

  @Override
  public boolean matches(LivingEntity entity) {
    return entities.contains(entity.getType());
  }

  @Override
  public IGenericLoader<? extends IJsonPredicate<LivingEntity>> getLoader() {
    return LOADER;
  }
}
