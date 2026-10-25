package slimeknights.mantle.datagen.client;

import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SpriteSourceProvider;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.Mantle;

import java.util.concurrent.CompletableFuture;

/** Adds sprite sources from Mantle */
@Internal
public class MantleSpriteSourceProvider extends SpriteSourceProvider {
  public MantleSpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
    super(output, lookupProvider, Mantle.modId, existingFileHelper);
  }

  @Override
  protected void gather() {
    atlas(BLOCKS_ATLAS).addSource(new DirectoryLister("fluid", "fluid" + '/'));
  }
}
