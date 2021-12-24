package slimeknights.mantle.registration.adapter;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Registry adapter for registering entity types
 */
@SuppressWarnings("unused")
public class EntityTypeRegistryAdapter extends RegistryAdapter<EntityType<?>> {
  /** @inheritDoc */
  public EntityTypeRegistryAdapter(IForgeRegistry<EntityType<?>> registry, String modId) {
    super(registry, modId);
  }

  /** @inheritDoc */
  public EntityTypeRegistryAdapter(IForgeRegistry<EntityType<?>> registry) {
    super(registry);
  }

  /**
   * Registers an entity type from a builder
   * @param builder  Builder instance
   * @param name     Type name
   * @param <T>      Entity type
   * @return  Registered entity type
   */
  public <T extends Entity> EntityType<T> register(EntityType.Builder<T> builder, String name) {
    return register(builder.build(resourceName(name)), name);
  }
}
