package slimeknights.mantle.data;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.registration.object.BlockItemTagKey;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/** Extension of {@link BlockTagsProvider} with support for creating tags with {@link BlockItemTagKey} */
public abstract class BlockTagProviderExtension extends BlockTagsProvider {
  public BlockTagProviderExtension(PackOutput output, CompletableFuture<Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
    super(output, lookupProvider, modId, existingFileHelper);
  }

  @Override
  protected BlockItemTagAppender tag(TagKey<Block> tag) {
    TagBuilder tagbuilder = this.getOrCreateRawBuilder(tag);
    return new BlockItemTagAppender(tagbuilder, this.keyExtractor, this.modId);
  }

  /** Creates a tag for a block item */
  protected BlockItemTagAppender tag(BlockItemTagKey tag) {
    return tag(tag.block());
  }

  /** Extension of {@link IntrinsicTagAppender} adding a helper to add a {@link BlockItemTagKey}. For simplicity it must be called first. */
  public static class BlockItemTagAppender extends IntrinsicTagAppender<Block> {
    protected BlockItemTagAppender(TagBuilder builder, Function<Block, ResourceKey<Block>> keyExtractor, String modId) {
      super(builder, keyExtractor, modId);
    }

    /** Adds the block item tag to the builder */
    public BlockItemTagAppender addTag(BlockItemTagKey tag) {
      addTag(tag.block());
      return this;
    }
  }
}
