package slimeknights.mantle.registration.object;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.mantle.registration.holder.DeferredEntity;

/**
 * Object holding an entity type and it's egg
 * <p>
 * For entities without spawn eggs, see {@link DeferredEntity}.
 */
public class EntityObject<T extends Entity> extends HolderWrapper<EntityType<?>,EntityType<T>> implements ItemLike {
  private final Holder<Item> spawnEgg;

  public EntityObject(DeferredHolder<EntityType<?>, ? extends EntityType<T>> entity, Holder<Item> spawnEgg) {
    super(entity);
    this.spawnEgg = spawnEgg;
  }

  @Override
  public Item asItem() {
    return spawnEgg.value();
  }
}
