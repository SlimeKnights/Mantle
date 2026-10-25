package slimeknights.mantle.datagen.tag;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.datagen.MantleTags;

import java.util.concurrent.CompletableFuture;

/** Provider for tags added by mantle, generally not useful for other mods */
@Internal
public class MantleItemTagProvider extends ItemTagsProvider {
  public MantleItemTagProvider(PackOutput output, CompletableFuture<Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
    super(output, lookupProvider, blockTags, Mantle.modId, existingFileHelper);
  }

  @Override
  protected void addTags(Provider pProvider) {
    tag(MantleTags.Items.EMPTY_FLUID_CONTAINERS).add(Items.BUCKET);
    tag(MantleTags.Items.FILLED_FLUID_CONTAINERS);
  }

  @Override
  public String getName() {
    return "Mantle Item Tag Provider";
  }
}
