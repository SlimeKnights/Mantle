package slimeknights.mantle.data.predicate.entity;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.NamedComponentRegistry;

/** Predicate matching a specific mob type */
public record MobTypePredicate(LivingEntityPredicate type) implements LivingEntityPredicate {
  /**
   * Registry of mob types, to allow addons to register types
   * TODO: support registering via IMC
   */
  public static final NamedComponentRegistry<LivingEntityPredicate> MOB_TYPES = new NamedComponentRegistry<>("Unknown mob type");
  /** Loader for a mob type predicate */
  public static RecordLoadable<MobTypePredicate> LOADER = RecordLoadable.create(MOB_TYPES.requiredField("mobs", MobTypePredicate::type), MobTypePredicate::new);

  /** Registers the vanilla mob type replacements. */
  public static void registerDefaults() {
    MOB_TYPES.register(ResourceLocation.withDefaultNamespace("undefined"), LivingEntityPredicate.simple(entity -> !entity.getType().is(EntityTypeTags.UNDEAD)
      && !entity.getType().is(EntityTypeTags.ARTHROPOD)
      && !entity.getType().is(EntityTypeTags.ILLAGER)
      && !entity.getType().is(EntityTypeTags.AQUATIC)));
    MOB_TYPES.register(ResourceLocation.withDefaultNamespace("undead"), tagged(EntityTypeTags.UNDEAD));
    MOB_TYPES.register(ResourceLocation.withDefaultNamespace("arthropod"), tagged(EntityTypeTags.ARTHROPOD));
    MOB_TYPES.register(ResourceLocation.withDefaultNamespace("illager"), tagged(EntityTypeTags.ILLAGER));
    MOB_TYPES.register(ResourceLocation.withDefaultNamespace("water"), tagged(EntityTypeTags.AQUATIC));
  }

  private static LivingEntityPredicate tagged(TagKey<EntityType<?>> tag) {
    return LivingEntityPredicate.simple(entity -> entity.getType().is(tag));
  }

  @Override
  public boolean matches(LivingEntity input) {
    return type.matches(input);
  }

  @Override
  public RecordLoadable<? extends LivingEntityPredicate> getLoader() {
    return LOADER;
  }
}
