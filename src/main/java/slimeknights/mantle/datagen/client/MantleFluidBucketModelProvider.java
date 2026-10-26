package slimeknights.mantle.datagen.client;

import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.client.FluidBucketModelProvider;
import slimeknights.mantle.fluid.MantleFluids;

/** Generates bucket item models for all new buckets */
@Internal
public class MantleFluidBucketModelProvider extends FluidBucketModelProvider {
  public MantleFluidBucketModelProvider(PackOutput packOutput) {
    super(packOutput, Mantle.modId);
  }

  @Override
  protected void addBuckets() {
    optionalFluid(MantleFluids.honey, false);
  }
}
