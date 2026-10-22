package slimeknights.mantle.registration.deferred;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.types.Type;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeknights.mantle.registration.holder.DeferredBlockEntity;
import slimeknights.mantle.registration.object.EnumObject;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** {@link DeferredRegister} for {@link BlockEntityType}. Has helpers to register from a constructor and block list directly. */
@SuppressWarnings("unused")  // API
public class BlockEntityTypeDeferredRegister extends DeferredRegister<BlockEntityType<?>> {
  public BlockEntityTypeDeferredRegister(String modID) {
    super(Registries.BLOCK_ENTITY_TYPE, modID);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected <I extends BlockEntityType<?>> DeferredHolder<BlockEntityType<?>, I> createHolder(ResourceKey<? extends Registry<BlockEntityType<?>>> registryKey, ResourceLocation key) {
    // this should be fine as long as no one uses the raw register and extends block entity type
    return (DeferredHolder<BlockEntityType<?>, I>) DeferredBlockEntity.createType(ResourceKey.create(registryKey, key));
  }

  /** @deprecated use {@link #registerType(String, Function)} or any of the specialized helpers. */
  @Deprecated
  @Override
  public <I extends BlockEntityType<?>> DeferredHolder<BlockEntityType<?>, I> register(String name, Supplier<? extends I> sup) {
    return super.register(name, sup);
  }

  /** @deprecated use {@link #registerType(String, Function)} or any of the specialized helpers. */
  @Deprecated
  @Override
  public <I extends BlockEntityType<?>> DeferredHolder<BlockEntityType<?>, I> register(String name, Function<ResourceLocation, ? extends I> func) {
    return super.register(name, func);
  }

  /**
   * Registers a block entity type returning a {@link DeferredBlockEntity}. Returns as {@link BlockEntityType}
   * @param name         Tile entity name
   * @param constructor  Constructor for the type
   * @param <T>      Block entity class
   * @return  Registry object instance
   */
  @SuppressWarnings("unchecked")
  public <T extends BlockEntity> DeferredBlockEntity<T> registerType(String name, Function<Type<?>,? extends BlockEntityType<T>> constructor) {
    return (DeferredBlockEntity<T>) register(name, id -> constructor.apply(Util.fetchChoiceType(References.BLOCK_ENTITY, id.toString())));
  }

  /**
   * Registers a tile entity type for a single block
   * @param name     Tile entity name
   * @param factory  Tile entity factory
   * @param block    Single block to add
   * @param <T>      Tile entity type
   * @return  Registry object instance
   */
  public <T extends BlockEntity> DeferredBlockEntity<T> register(String name, BlockEntitySupplier<? extends T> factory, Supplier<? extends Block> block) {
    return registerType(name, type -> BlockEntityType.Builder.<T>of(factory, block.get()).build(type));
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
  public <T extends BlockEntity> DeferredBlockEntity<T> register(String name, BlockEntitySupplier<? extends T> factory, EnumObject<?, ? extends Block> blocks) {
    return registerType(name, type -> new BlockEntityType<>(factory, ImmutableSet.copyOf(blocks.values()), type));
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
  public <T extends BlockEntity> DeferredBlockEntity<T> register(String name, BlockEntitySupplier<? extends T> factory, Consumer<ImmutableSet.Builder<Block>> blockCollector) {
    return registerType(name, type ->  {
      ImmutableSet.Builder<Block> blocks = ImmutableSet.builder();
      blockCollector.accept(blocks);
      return new BlockEntityType<>(factory, blocks.build(), type);
    });
  }
}
