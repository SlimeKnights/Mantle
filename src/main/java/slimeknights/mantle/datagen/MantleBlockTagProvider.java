package slimeknights.mantle.datagen;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.Mantle;

import java.util.concurrent.CompletableFuture;

import static slimeknights.mantle.datagen.MantleTags.Blocks.ATTACHED_GAUGES;
import static slimeknights.mantle.datagen.MantleTags.Blocks.GAUGES;
import static slimeknights.mantle.datagen.MantleTags.Blocks.GAUGE_TANKS;

/** Provider for tags added by mantle, generally not useful for other mods */
@Internal
public class MantleBlockTagProvider extends BlockTagsProvider {
  public MantleBlockTagProvider(PackOutput output, CompletableFuture<Provider> holders, ExistingFileHelper existingFileHelper) {
    super(output, holders,  Mantle.modId, existingFileHelper);
  }

  @Override
  protected void addTags(Provider pProvider) {
    this.tag(GAUGES).addOptionalTag(ATTACHED_GAUGES.location()).addOptionalTag(GAUGE_TANKS.location());
  }

  @Override
  public String getName() {
    return "Mantle Block Tag Provider";
  }
}
