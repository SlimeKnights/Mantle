package slimeknights.mantle.registration.adapter;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.mojang.datafixers.types.Type;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.TypeReferences;
import net.minecraftforge.registries.IForgeRegistry;
import slimeknights.mantle.registration.object.EnumObject;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Registry adapter for tile entity types with helpers for adding blocks
 */
@SuppressWarnings("unused")
public class TileEntityTypeRegistryAdapter extends RegistryAdapter<TileEntityType<?>> {
  /** @inheritDoc */
  public TileEntityTypeRegistryAdapter(IForgeRegistry<TileEntityType<?>> registry, String modId) {
    super(registry, modId);
  }

  /** @inheritDoc */
  public TileEntityTypeRegistryAdapter(IForgeRegistry<TileEntityType<?>> registry) {
    super(registry);
  }

  /**
   * Gets the data fixer type for the tile entity instance
   * @param name  Tile entity name
   * @return  Data fixer type
   */
  @Nullable
  private Type<?> getType(String name) {
    return Util.fetchChoiceType(TypeReferences.BLOCK_ENTITY, resourceName(name));
  }

  /**
   * Registers a tile entity type for a single block
   * @param factory  Tile entity factory
   * @param block    Single block to add
   * @param name     Tile entity name
   * @param <T>      Tile entity type
   * @return  Registry object instance
   */
  @SuppressWarnings("ConstantConditions")
  public <T extends TileEntity> TileEntityType<T> register(Supplier<? extends T> factory, Block block, String name) {
    return register(TileEntityType.Builder.<T>of(factory, block).build(getType(name)), name);
  }

  /**
   * Registers a new tile entity type using a tile entity factory and an enum object
   * @param name     Tile entity name
   * @param factory  Tile entity factory
   * @param blocks   Enum object instance
   * @param <T>      Tile entity type
   * @return  Tile entity type registry object
   */
  @SuppressWarnings("ConstantConditions")
  public <T extends TileEntity> TileEntityType<T> register(Supplier<? extends T> factory, EnumObject<?, ? extends Block> blocks, String name) {
    return register(new TileEntityType<>(factory, ImmutableSet.copyOf(blocks.values()), getType(name)), name);
  }

  /**
   * Registers a new tile entity type using a tile entity factory and an immutable set builder
   * @param factory          Tile entity factory
   * @param name             Tile entity name
   * @param blockCollector   Function to get blocks for the list
   * @param <T>              Tile entity type
   * @return  Tile entity type registry object
   */
  @SuppressWarnings("ConstantConditions")
  public <T extends TileEntity> TileEntityType<T> register(Supplier<? extends T> factory, String name, Consumer<Builder<Block>> blockCollector) {
    ImmutableSet.Builder<Block> blocks = new ImmutableSet.Builder<>();
    blockCollector.accept(blocks);
    return register(new TileEntityType<>(factory, blocks.build(), getType(name)), name);
  }
}
