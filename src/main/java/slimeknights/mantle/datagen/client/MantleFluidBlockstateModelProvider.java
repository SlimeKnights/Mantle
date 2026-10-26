package slimeknights.mantle.datagen.client;

import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.client.FluidBlockstateModelProvider;
import slimeknights.mantle.fluid.MantleFluids;

/** Generates block state models for all new fluid blocks */
@Internal
public class MantleFluidBlockstateModelProvider extends FluidBlockstateModelProvider {
  public MantleFluidBlockstateModelProvider(PackOutput packOutput) {
    super(packOutput, Mantle.modId);
  }

  @Override
  protected void addModels() {
    // since all the fluids are optional, just manually list them instead of a registry scan
    block(MantleFluids.honey);
    block(MantleFluids.beetrootSoup);
    block(MantleFluids.mushroomStew);
    block(MantleFluids.rabbitStew);
  }
}
