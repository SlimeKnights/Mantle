package slimeknights.mantle.data.predicate.entity;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;

/**
 * Predicate matching an entity tag
 */
public record TagEntityPredicate(TagKey<EntityType<?>> tag) implements LivingEntityPredicate {
  public static final RecordLoadable<TagEntityPredicate> LOADER = RecordLoadable.create(Loadables.ENTITY_TYPE_TAG.field("tag", TagEntityPredicate::tag), TagEntityPredicate::new);

  @Override
  public boolean matches(LivingEntity entity) {
    return entity.getType().is(tag);
  }

  @Override
  public IGenericLoader<? extends LivingEntityPredicate> getLoader() {
    return LOADER;
  }
}
