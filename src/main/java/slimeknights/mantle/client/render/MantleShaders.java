package slimeknights.mantle.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.config.Config;

import javax.annotation.Nullable;
import java.io.IOException;

/** Handles any custom shaders registered by Mantle. */
@EventBusSubscriber(modid = Mantle.modId, value = Dist.CLIENT)
public class MantleShaders {
  /** Shader used for blocks in structures to force them fullbright. Based on ... */
  @Nullable
  @Getter
  private static ShaderInstance blockFullBrightShader;
  /** Shader used for fluids in block entity renderers. Based on {@link GameRenderer#positionColorTexLightmapShader} nad {@link GameRenderer#rendertypeEntityTranslucentCullShader} */
  @Nullable
  @Getter
  private static ShaderInstance fluidShader;

  /** Gets the shader to use for {@link MantleRenderTypes#FLUID_SHADER}, checking the config option to select which shader to use. */
  @Nullable
  public static ShaderInstance getConfiguredFluidShader() {
    if (Config.ENABLE_FLUID_FOG_FIX.get()) {
      return fluidShader;
    }
    return Config.FLUID_USE_TEXT_SHADER.get() ? GameRenderer.getRendertypeTextShader() : GameRenderer.getPositionColorTexLightmapShader();
  }

  @SubscribeEvent
  static void registerShaders(RegisterShadersEvent event) throws IOException {
    event.registerShader(
      new ShaderInstance(event.getResourceProvider(), Mantle.getResource("block_fullbright"), DefaultVertexFormat.BLOCK),
      shader -> blockFullBrightShader = shader
    );
    event.registerShader(
      new ShaderInstance(event.getResourceProvider(), Mantle.getResource("fluid"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
      shader -> fluidShader = shader
    );
  }
}
