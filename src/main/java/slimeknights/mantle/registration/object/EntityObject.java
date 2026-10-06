package slimeknights.mantle.registration.object;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Object holding an entity and it's egg */
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
