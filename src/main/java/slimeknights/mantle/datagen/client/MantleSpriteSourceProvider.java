package slimeknights.mantle.datagen.client;

import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SpriteSourceProvider;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.Mantle;

/** Adds sprite sources from Mantle */
@Internal
public class MantleSpriteSourceProvider extends SpriteSourceProvider {
  public MantleSpriteSourceProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
    super(output, existingFileHelper, Mantle.modId);
  }

  @Override
  protected void addSources() {
    atlas(BLOCKS_ATLAS).addSource(new DirectoryLister("fluid", "fluid" + '/'));
  }
}
