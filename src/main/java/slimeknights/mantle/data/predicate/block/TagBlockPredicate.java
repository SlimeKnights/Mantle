package slimeknights.mantle.data.predicate.block;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.data.loader.TagKeyLoader;

/**
 * Modifier matching a block tag
 */
@RequiredArgsConstructor
public class TagBlockPredicate implements BlockPredicate {
  public static final TagKeyLoader<Block,TagBlockPredicate> LOADER = new TagKeyLoader<>(Registry.BLOCK_REGISTRY, TagBlockPredicate::new, c -> c.tag);
  private final TagKey<Block> tag;

  @Override
  public boolean matches(BlockState state) {
    return state.is(tag);
  }

  @Override
  public IGenericLoader<? extends BlockPredicate> getLoader() {
    return LOADER;
  }
}
