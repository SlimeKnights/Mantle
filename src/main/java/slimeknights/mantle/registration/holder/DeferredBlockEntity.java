package slimeknights.mantle.registration.holder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

/** {@link DeferredHolder} for a {@link BlockEntityType}. Requires using {@link BlockEntityType} for the supplier. */
public class DeferredBlockEntity<T extends BlockEntity> extends DeferredHolder<BlockEntityType<?>,BlockEntityType<T>> {
  protected DeferredBlockEntity(ResourceKey<BlockEntityType<?>> key) {
    super(key);
  }

  /** Creates a new instance for the given resource location */
  public static <T extends BlockEntity> DeferredBlockEntity<T> createType(ResourceLocation key) {
    return createType(ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, key));
  }

  /** Creates a new instance for the given resource key */
  public static <T extends BlockEntity> DeferredBlockEntity<T> createType(ResourceKey<BlockEntityType<?>> key) {
    return new DeferredBlockEntity<>(key);
  }
}
