package slimeknights.mantle.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.mantle.Mantle;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = Mantle.modId, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MantleShaders {

  private static ShaderInstance blockFullBrightShader;

  @SubscribeEvent
  static void registerShaders(RegisterShadersEvent event) throws IOException {
    event.registerShader(
      new ShaderInstance(event.getResourceManager(), Mantle.getResource("block_fullbright"), DefaultVertexFormat.BLOCK),
      shader -> blockFullBrightShader = shader
    );
  }

  public static ShaderInstance getBlockFullBrightShader() {
    return blockFullBrightShader;
  }
}
