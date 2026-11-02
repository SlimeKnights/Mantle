package slimeknights.mantle.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;
import java.io.IOException;

/** Handles any custom shaders registered by Mantle. */
@EventBusSubscriber(modid = Mantle.modId, value = Dist.CLIENT)
public class MantleShaders {
  /** Shader used for blocks in structures to force them fullbright. Based on ... */
  @Nullable
  @Getter
  private static ShaderInstance blockFullBrightShader;

  @SubscribeEvent
  static void registerShaders(RegisterShadersEvent event) throws IOException {
    event.registerShader(
      new ShaderInstance(event.getResourceProvider(), Mantle.getResource("block_fullbright"), DefaultVertexFormat.BLOCK),
      shader -> blockFullBrightShader = shader
    );
  }
}
