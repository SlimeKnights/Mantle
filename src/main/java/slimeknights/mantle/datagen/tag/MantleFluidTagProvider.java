package slimeknights.mantle.datagen.tag;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.fluid.MantleFluids;
import slimeknights.mantle.registration.object.FlowingFluidObject;
import slimeknights.mantle.registration.object.FluidObject;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static slimeknights.mantle.datagen.MantleTags.Fluids.SOUP;

/** Provider for tags added by mantle, generally not useful for other mods */
@SuppressWarnings("unchecked")
@Internal
public class MantleFluidTagProvider extends FluidTagsProvider {
  public MantleFluidTagProvider(PackOutput output, CompletableFuture<Provider> holders, ExistingFileHelper existingFileHelper) {
    super(output, holders,  Mantle.modId, existingFileHelper);
  }

  @Override
  protected void addTags(Provider pProvider) {
    this.tag(SOUP)
      .addOptionalTag(Tags.Fluids.BEETROOT_SOUP)
      .addOptionalTag(Tags.Fluids.MUSHROOM_STEW)
      .addOptionalTag(Tags.Fluids.RABBIT_STEW);
    addTags(MantleFluids.potion);
    addTags(MantleFluids.powderSnow);
    addTags(MantleFluids.honey);
    this.tag(MantleTags.Fluids.HIDE_IN_DISPLAY)
      .addOptionalTags(Tags.Fluids.POTION, Tags.Fluids.HIDDEN_FROM_RECIPE_VIEWERS);
  }

  @Override
  public String getName() {
    return "Mantle Fluid Tag Provider";
  }

  /** Tags a fluid object in its common tag */
  private void addTags(FluidObject<?> fluid) {
    tag(Objects.requireNonNull(fluid.getCommonTag())).addOptional(fluid.id());
  }

  /** Tags a fluid object in its common tag */
  private void addTags(FlowingFluidObject<?> fluid) {
    tag(Objects.requireNonNull(fluid.getCommonTag())).addOptionalTag(fluid.getLocalTag());
    tag(fluid.getLocalTag()).addOptional(fluid.id()).addOptional(fluid.id().withPrefix("flowing_"));
  }
}
