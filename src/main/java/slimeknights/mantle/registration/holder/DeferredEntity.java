package slimeknights.mantle.registration.holder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.mantle.registration.object.EntityObject;

/**
 * {@link DeferredHolder} for a {@link EntityType}. Requires using {@link EntityType} for the supplier.
 * <p>
 * For objects with a spawn egg, see {@link EntityObject}.
 */
public class DeferredEntity<T extends Entity> extends DeferredHolder<EntityType<?>,EntityType<T>> {
  protected DeferredEntity(ResourceKey<EntityType<?>> key) {
    super(key);
  }

  /** Creates a new instance for the given resource location */
  public static <T extends Entity> DeferredEntity<T> createType(ResourceLocation key) {
    return createType(ResourceKey.create(Registries.ENTITY_TYPE, key));
  }

  /** Creates a new instance for the given resource key */
  public static <T extends Entity> DeferredEntity<T> createType(ResourceKey<EntityType<?>> key) {
    return new DeferredEntity<>(key);
  }
}
