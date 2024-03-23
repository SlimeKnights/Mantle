package slimeknights.mantle.data.predicate.block;

import com.google.common.collect.ImmutableSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.RegistryPredicateRegistry;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.SingletonLoader;

import java.util.List;
import java.util.function.Predicate;

/**
 * Simple serializable block predicate
 */
public interface BlockPredicate extends IJsonPredicate<BlockState> {
  /** Predicate that matches any block */
  BlockPredicate ANY = simple(state -> true);
  /** Loader for block state predicates */
  RegistryPredicateRegistry<Block,BlockState> LOADER = new RegistryPredicateRegistry<>("Block Predicate", ANY, Loadables.BLOCK, BlockState::getBlock, "blocks", Loadables.BLOCK_TAG, (tag, state) -> state.is(tag));

  /** Gets an inverted condition */
  @Override
  default IJsonPredicate<BlockState> inverted() {
    return LOADER.invert(this);
  }


  /* Singleton */

  /** Predicate that matches blocks with no harvest tool */
  BlockPredicate REQUIRES_TOOL = simple(BlockStateBase::requiresCorrectToolForDrops);

  /** Creates a new simple predicate */
  static BlockPredicate simple(Predicate<BlockState> predicate) {
    return SingletonLoader.singleton(loader -> new BlockPredicate() {
      @Override
      public boolean matches(BlockState state) {
        return predicate.test(state);
      }

      @Override
      public IGenericLoader<? extends BlockPredicate> getLoader() {
        return loader;
      }
    });
  }


  /* Helper methods */

  /** Creates a block set predicate */
  static IJsonPredicate<BlockState> set(Block... blocks) {
    return LOADER.setOf(ImmutableSet.copyOf(blocks));
  }

  /** Creates a tag predicate */
  static IJsonPredicate<BlockState> tag(TagKey<Block> tag) {
    return LOADER.tag(tag);
  }

  /** Creates an and predicate */
  @SafeVarargs
  static IJsonPredicate<BlockState> and(IJsonPredicate<BlockState>... predicates) {
    return LOADER.and(List.of(predicates));
  }

  /** Creates an or predicate */
  @SafeVarargs
  static IJsonPredicate<BlockState> or(IJsonPredicate<BlockState>... predicates) {
    return LOADER.or(List.of(predicates));
  }
}
