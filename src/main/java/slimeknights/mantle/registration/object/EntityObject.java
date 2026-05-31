package slimeknights.mantle.registration.object;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

/** Object holding an entity and it's egg */
@RequiredArgsConstructor
public class EntityObject<T extends Entity> implements Supplier<EntityType<T>>, ItemLike, IdAwareObject {
  @Getter
  private final ResourceLocation id;
  private final Supplier<? extends EntityType<T>> type;
  private final Supplier<? extends SpawnEggItem> spawnEgg;

  public EntityObject(DeferredHolder<EntityType<?>, ? extends EntityType<T>> type, Supplier<? extends SpawnEggItem> spawnEgg) {
    this.id = type.getId();
    this.type = type;
    this.spawnEgg = spawnEgg;
  }

  @Override
  public EntityType<T> get() {
    return type.get();
  }

  @Override
  public Item asItem() {
    return spawnEgg.get();
  }
}
