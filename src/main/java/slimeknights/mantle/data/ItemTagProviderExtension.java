package slimeknights.mantle.data;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.registration.object.BlockItemTagKey;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/** Extension of {@link ItemTagsProvider} with support for copying using {@link BlockItemTagKey} */
@SuppressWarnings("unused")  // API
public abstract class ItemTagProviderExtension extends ItemTagsProvider {
  public ItemTagProviderExtension(PackOutput output, CompletableFuture<Provider> lookupProvider, CompletableFuture<TagLookup<Item>> parentProvider, CompletableFuture<TagLookup<Block>> blockTags, String modId, @Nullable ExistingFileHelper existingFileHelper) {
    super(output, lookupProvider, parentProvider, blockTags, modId, existingFileHelper);
  }

  public ItemTagProviderExtension(PackOutput output, CompletableFuture<Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, String modId, @Nullable ExistingFileHelper existingFileHelper) {
    super(output, lookupProvider, blockTags, modId, existingFileHelper);
  }

  /** Copies the block tag to the item tag in the given tag key pair. */
  protected void copy(BlockItemTagKey tags) {
    super.copy(tags.block(), tags.item());
  }

  /** Copies the block tags to the item tags in the given tag key pairs. */
  protected void copy(BlockItemTagKey... tags) {
    for (BlockItemTagKey tag : tags) {
      copy(tag);
    }
  }

  @Override
  protected BlockItemTagAppender tag(TagKey<Item> tag) {
    TagBuilder tagbuilder = this.getOrCreateRawBuilder(tag);
    return new BlockItemTagAppender(tagbuilder, this.keyExtractor, this.modId);
  }

  /** Creates a tag for a block item */
  protected BlockItemTagAppender tag(BlockItemTagKey tag) {
    return tag(tag.item());
  }

  /** Extension of {@link IntrinsicTagAppender} adding a helper to add a {@link BlockItemTagKey}. For simplicity, it must be called first. */
  public static class BlockItemTagAppender extends IntrinsicTagAppender<Item> {
    protected BlockItemTagAppender(TagBuilder builder, Function<Item, ResourceKey<Item>> keyExtractor, String modId) {
      super(builder, keyExtractor, modId);
    }

    /** Adds the block item tag to the builder */
    @CanIgnoreReturnValue
    public BlockItemTagAppender addTag(BlockItemTagKey tag) {
      addTag(tag.item());
      return this;
    }

    /** Adds the block item tag to the builder */
    @CanIgnoreReturnValue
    public BlockItemTagAppender addTags(BlockItemTagKey... tags) {
      for (BlockItemTagKey tag : tags) {
        addTag(tag);
      }
      return this;
    }
  }
}
