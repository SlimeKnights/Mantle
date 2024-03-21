package slimeknights.mantle.data.predicate.block;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;

/**
 * Modifier matching a block tag
 */
public record TagBlockPredicate(TagKey<Block> tag) implements BlockPredicate {
  public static final RecordLoadable<TagBlockPredicate> LOADER = RecordLoadable.create(Loadables.BLOCK_TAG.field("tag", TagBlockPredicate::tag), TagBlockPredicate::new);

  @Override
  public boolean matches(BlockState state) {
    return state.is(tag);
  }

  @Override
  public IGenericLoader<? extends BlockPredicate> getLoader() {
    return LOADER;
  }
}
