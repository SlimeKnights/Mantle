package slimeknights.mantle.registration.deferred;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.types.Type;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.registration.object.EnumObject;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Deferred register to register tile entity instances
 */
public class TileEntityTypeDeferredRegister extends DeferredRegisterWrapper<BlockEntityType<?>> {
  public TileEntityTypeDeferredRegister(String modID) {
    super(ForgeRegistries.TILE_ENTITIES, modID);
  }

  /**
   * Gets the data fixer type for the tile entity instance
   * @param name  Tile entity name
   * @return  Data fixer type
   */
  @Nullable
  private Type<?> getType(String name) {
    return Util.fetchChoiceType(References.BLOCK_ENTITY, resourceName(name));
  }

  /**
   * Registers a tile entity type for a single block
   * @param name     Tile entity name
   * @param factory  Tile entity factory
   * @param block    Single block to add
   * @param <T>      Tile entity type
   * @return  Registry object instance
   */
  @SuppressWarnings("ConstantConditions")
  public <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String name, Supplier<? extends T> factory, Supplier<? extends Block> block) {
    return register.register(name, () ->  BlockEntityType.Builder.<T>of(factory, block.get()).build(getType(name)));
  }

  /**
   * Registers a new tile entity type using a tile entity factory and a block supplier
   * @param name     Tile entity name
   * @param factory  Tile entity factory
   * @param blocks   Enum object
   * @param <T>      Tile entity type
   * @return  Tile entity type registry object
   */
  @SuppressWarnings("ConstantConditions")
  public <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String name, Supplier<? extends T> factory, EnumObject<?, ? extends Block> blocks) {
    return register.register(name, () ->  new BlockEntityType<>(factory, ImmutableSet.copyOf(blocks.values()), getType(name)));
  }

  /**
   * Registers a new tile entity type using a tile entity factory and a block supplier
   * @param name             Tile entity name
   * @param factory          Tile entity factory
   * @param blockCollector   Function to get block list
   * @param <T>              Tile entity type
   * @return  Tile entity type registry object
   */
  @SuppressWarnings("ConstantConditions")
  public <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String name, Supplier<? extends T> factory, Consumer<ImmutableSet.Builder<Block>> blockCollector) {
    return register.register(name, () ->  {
      ImmutableSet.Builder<Block> blocks = new ImmutableSet.Builder<>();
      blockCollector.accept(blocks);
      return new BlockEntityType<>(factory, blocks.build(), getType(name));
    });
  }
}
