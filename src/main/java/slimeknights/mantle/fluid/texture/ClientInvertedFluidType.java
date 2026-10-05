package slimeknights.mantle.fluid.texture;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidType;

/** Client logic for {@link slimeknights.mantle.fluid.InvertedFluidType} */
public class ClientInvertedFluidType extends ClientTextureFluidType {
  private ResourceLocation lastFlowing;
  private ResourceLocation invertedFlowing;
  public ClientInvertedFluidType(FluidType type) {
    super(type);
  }

  @Override
  public ResourceLocation getFlowingTexture(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
    ResourceLocation flowing = getFlowingTexture();
    if (flowing == lastFlowing) {
      return invertedFlowing;
    }
    invertedFlowing = flowing.withSuffix("_inverted");
    lastFlowing = flowing;
    return invertedFlowing;
  }
}
