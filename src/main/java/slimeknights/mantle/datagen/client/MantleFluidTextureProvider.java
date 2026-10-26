package slimeknights.mantle.datagen.client;

import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.fluid.MantleFluids;
import slimeknights.mantle.fluid.texture.AbstractFluidTextureProvider;
import slimeknights.mantle.fluid.texture.FluidTexture;
import slimeknights.mantle.registration.object.FluidObject;

/** Adds fluid textures for Mantle */
@Internal
public class MantleFluidTextureProvider extends AbstractFluidTextureProvider {
  public MantleFluidTextureProvider(PackOutput packOutput) {
    super(packOutput, Mantle.modId);
  }

  /** Wraps the texture with the fluid folder and a suffix of underscore for the two textures. */
  private FluidTexture.Builder root(FluidObject<?> fluid) {
    return texture(fluid).wrapId("fluid/", "_").still().flowing();
  }

  @Override
  public void addTextures() {
    root(MantleFluids.potion).color(0xff385dc6);
    root(MantleFluids.powderSnow);
  }

  @Override
  public String getName() {
    return "Mantle fluid texture provider";
  }
}
