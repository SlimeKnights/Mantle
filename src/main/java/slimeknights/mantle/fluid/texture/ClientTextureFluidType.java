package slimeknights.mantle.fluid.texture;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer.FogMode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Vector3f;
import slimeknights.mantle.client.render.FluidRenderer;

import javax.annotation.Nullable;

/** Implementation of {@link IClientFluidTypeExtensions} using {@link FluidTexture} */
@RequiredArgsConstructor
public class ClientTextureFluidType implements IClientFluidTypeExtensions {
  protected final FluidType type;
  private Vector3f fogColor;

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

  @Override
  public void renderOverlay(Minecraft mc, PoseStack poseStack) {
    FluidTexture data = FluidTextureManager.getData(type);
    ResourceLocation camera = data.camera();
    if (camera != null) {
      FluidRenderer.renderCamera(mc, poseStack, camera, data.cameraOpacity(), data.color());
    }
  }

  @Override
  public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
    // nothing to do if fog color is white
    int fluidColor = FluidTextureManager.getData(type).fogColor();
    if (fluidColor != -1) {
      // cache the vector for fog color to reduce computation time
      if (fogColor == null) {
        fogColor = new Vector3f(FastColor.ARGB32.red(fluidColor) / 255f, FastColor.ARGB32.green(fluidColor) / 255f, FastColor.ARGB32.blue(fluidColor) / 255f);
      }
      fluidFogColor.x *= fogColor.x;
      fluidFogColor.y *= fogColor.y;
      fluidFogColor.z *= fogColor.z;
    }
    return fluidFogColor;
  }

  @Override
  public void modifyFogRender(Camera camera, FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
    FluidTexture data = FluidTextureManager.getData(type);
    FogShape overrideShape = data.fogShape();
    if (overrideShape != null) {
      if (overrideShape != shape) {
        RenderSystem.setShaderFogShape(overrideShape);
      }
      float start = data.fogStart();
      if (start < nearDistance) {
        RenderSystem.setShaderFogStart(start);
      }
      float end = data.fogEnd();
      if (end < farDistance) {
        RenderSystem.setShaderFogEnd(end);
      }
    }
  }
}
