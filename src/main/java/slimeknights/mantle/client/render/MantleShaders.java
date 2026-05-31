package slimeknights.mantle.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.config.Config;

import javax.annotation.Nullable;
import java.io.IOException;

/** Handles any custom shaders registered by Mantle. */
@EventBusSubscriber(modid = Mantle.modId, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class MantleShaders {
  @Nullable
  @Getter
  private static ShaderInstance blockFullBrightShader;
  @Nullable
  @Getter
  private static ShaderInstance fluidShader;

  /** Gets the shader to use for {@link MantleRenderTypes#FLUID_SHADER}, checking the config option to select which shader to use. */
  @Nullable
  public static ShaderInstance getConfiguredFluidShader() {
    return Config.ENABLE_FLUID_FOG_FIX.get() ? fluidShader : GameRenderer.getPositionColorTexLightmapShader();
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
