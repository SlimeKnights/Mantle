package slimeknights.mantle.data;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.registration.object.BlockItemTagKey;

import java.util.concurrent.CompletableFuture;

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
}
