package slimeknights.mantle.fluid.texture;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

import javax.annotation.Nullable;

/** Implementation of {@link IClientFluidTypeExtensions} using {@link FluidTexture} */
@SuppressWarnings("ClassCanBeRecord") // Want to allow extending to override other properties
@RequiredArgsConstructor
public class ClientTextureFluidType implements IClientFluidTypeExtensions {
  protected final FluidType type;

  @Override
  public int getTintColor() {
    return FluidTextureManager.getColor(type);
  }

  @Override
  public ResourceLocation getStillTexture() {
    return FluidTextureManager.getStillTexture(type);
  }

  @Override
  public ResourceLocation getFlowingTexture() {
    return FluidTextureManager.getFlowingTexture(type);
  }

  @Nullable
  @Override
  public ResourceLocation getOverlayTexture() {
    return FluidTextureManager.getOverlayTexture(type);
  }

  @Nullable
  @Override
  public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
    return FluidTextureManager.getCameraTexture(type);
  }
}
