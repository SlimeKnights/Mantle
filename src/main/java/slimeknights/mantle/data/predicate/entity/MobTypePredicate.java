package slimeknights.mantle.data.predicate.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.data.registry.NamedComponentRegistry;

/** Predicate matching a specific mob type */
public record MobTypePredicate(MobType type) implements LivingEntityPredicate {
  /**
   * Registry of mob types, to allow addons to register types
   * TODO: support registering via IMC
   */
  public static final NamedComponentRegistry<MobType> MOB_TYPES = new NamedComponentRegistry<>("Unknown mob type");
  /** Loader for a mob type predicate */
  public static RecordLoadable<MobTypePredicate> LOADER = RecordLoadable.create(MOB_TYPES.requiredField("mobs", MobTypePredicate::type), MobTypePredicate::new);

  @Override
  public boolean matches(LivingEntity input) {
    return input.getMobType() == type;
  }

  @Override
  public IGenericLoader<? extends LivingEntityPredicate> getLoader() {
    return LOADER;
  }
}
