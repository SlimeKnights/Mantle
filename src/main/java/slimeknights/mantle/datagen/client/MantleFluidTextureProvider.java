package slimeknights.mantle.datagen.client;

import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.fluid.MantleFluids;
import slimeknights.mantle.fluid.texture.AbstractFluidTextureProvider;

/** Adds fluid textures for Mantle */
@Internal
public class MantleFluidTextureProvider extends AbstractFluidTextureProvider {
  public MantleFluidTextureProvider(PackOutput packOutput) {
    super(packOutput, Mantle.modId);
  }

  @Override
  public void addTextures() {
    texture(MantleFluids.potion).wrapId("fluid/", "_").still().flowing().color(0xff385dc6);
  }

  @Override
  public String getName() {
    return "Mantle fluid texture provider";
  }
}
