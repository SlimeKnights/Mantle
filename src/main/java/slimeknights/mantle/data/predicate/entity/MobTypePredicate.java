package slimeknights.mantle.data.predicate.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.NamedComponentRegistry;

/**
 * Predicate matching entities by type tag.
 * In 1.21+, MobType was removed and replaced with entity type tags like #minecraft:undead.
 */
public record MobTypePredicate(TagKey<EntityType<?>> typeTag) implements LivingEntityPredicate {
  private static final TagKey<EntityType<?>> LEGACY_WATER = entityTag("water");
  private static final TagKey<EntityType<?>> LEGACY_UNDEFINED = entityTag("undefined");

  /**
   * Registry of mob type tag names for compatibility.
   * Maps legacy names like "undead" to entity type tags.
   */
  public static final NamedComponentRegistry<TagKey<EntityType<?>>> MOB_TYPES = new NamedComponentRegistry<>("Unknown mob type");
  
  /** Loader for a mob type predicate */
  public static RecordLoadable<MobTypePredicate> LOADER = RecordLoadable.create(
    MOB_TYPES.requiredField("mobs", MobTypePredicate::typeTag), 
    MobTypePredicate::new
  );

  /** Helper to create entity type tags */
  private static TagKey<EntityType<?>> entityTag(String name) {
    return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.withDefaultNamespace(name));
  }

  /** Initialize the default mob type tags */
  public static void init() {
    // Map legacy mob type names to entity type tags
    MOB_TYPES.register(ResourceLocation.withDefaultNamespace("undead"), EntityTypeTags.UNDEAD);
    MOB_TYPES.register(ResourceLocation.withDefaultNamespace("arthropod"), EntityTypeTags.ARTHROPOD);
    MOB_TYPES.register(ResourceLocation.withDefaultNamespace("illager"), EntityTypeTags.ILLAGER);
    MOB_TYPES.register(ResourceLocation.withDefaultNamespace("aquatic"), EntityTypeTags.AQUATIC);

    // Legacy compatibility: 1.20's MobType used "water" and "undefined"
    MOB_TYPES.register(ResourceLocation.withDefaultNamespace("water"), LEGACY_WATER);
    MOB_TYPES.register(ResourceLocation.withDefaultNamespace("undefined"), LEGACY_UNDEFINED);
  }

  @Override
  public boolean matches(LivingEntity input) {
    EntityType<?> type = input.getType();
    // Legacy compatibility: emulate removed MobType semantics
    if (typeTag.equals(LEGACY_WATER)) {
      return type.is(EntityTypeTags.AQUATIC);
    }
    if (typeTag.equals(LEGACY_UNDEFINED)) {
      return !type.is(EntityTypeTags.UNDEAD)
             && !type.is(EntityTypeTags.ARTHROPOD)
             && !type.is(EntityTypeTags.ILLAGER)
             && !type.is(EntityTypeTags.AQUATIC);
    }
    return type.is(typeTag);
  }

  @Override
  public RecordLoadable<? extends LivingEntityPredicate> getLoader() {
    return LOADER;
  }
}
