package slimeknights.mantle.data.predicate.block;

import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.data.GenericLoaderRegistry;
import slimeknights.mantle.data.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.data.GenericLoaderRegistry.SingletonLoader;
import slimeknights.mantle.data.predicate.AndJsonPredicate;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.InvertedJsonPredicate;
import slimeknights.mantle.data.predicate.NestedJsonPredicateLoader;
import slimeknights.mantle.data.predicate.OrJsonPredicate;

import java.util.function.Predicate;

/**
 * Simple serializable block predicate
 */
public interface BlockPredicate extends IJsonPredicate<BlockState> {
  /** Predicate that matches any block */
  BlockPredicate ANY = simple(state -> true);
  /** Loader for block state predicates */
  GenericLoaderRegistry<IJsonPredicate<BlockState>> LOADER = new GenericLoaderRegistry<>(ANY, true);
  /** Loader for inverted conditions */
  InvertedJsonPredicate.Loader<BlockState> INVERTED = new InvertedJsonPredicate.Loader<>(LOADER);
  /** Loader for and conditions */
  NestedJsonPredicateLoader<BlockState,AndJsonPredicate<BlockState>> AND = AndJsonPredicate.createLoader(LOADER, INVERTED);
  /** Loader for or conditions */
  NestedJsonPredicateLoader<BlockState,OrJsonPredicate<BlockState>> OR = OrJsonPredicate.createLoader(LOADER, INVERTED);

  /** Gets an inverted condition */
  @Override
  default IJsonPredicate<BlockState> inverted() {
    return INVERTED.create(this);
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
      public IGenericLoader<? extends IJsonPredicate<BlockState>> getLoader() {
        return loader;
      }
    });
  }
}
