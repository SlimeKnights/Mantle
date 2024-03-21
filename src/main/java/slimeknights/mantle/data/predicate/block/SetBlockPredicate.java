package slimeknights.mantle.data.predicate.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;

import java.util.Set;

/**
 * Modifier matching a block
 */
public record SetBlockPredicate(Set<Block> blocks) implements BlockPredicate {
  public static final IGenericLoader<SetBlockPredicate> LOADER = RecordLoadable.create(Loadables.BLOCK.set().field("blocks", SetBlockPredicate::blocks), SetBlockPredicate::new);

  @Override
  public boolean matches(BlockState state) {
    return blocks.contains(state.getBlock());
  }

  @Override
  public IGenericLoader<? extends BlockPredicate> getLoader() {
    return LOADER;
  }
}
