package slimeknights.mantle.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import slimeknights.mantle.Mantle;

/**
 * Class for render types defined by Mantle
 */
public class MantleRenderTypes extends RenderType {
  private MantleRenderTypes(String name, VertexFormat format, Mode mode, int bufferSize, boolean useDelegate, boolean needsSorting, Runnable setupTaskIn, Runnable clearTaskIn) {
    super(name, format, mode, bufferSize, useDelegate, needsSorting, setupTaskIn, clearTaskIn);
  }

  /** Render type for cutout without cull (i.e. two-sided quads). Used for the fluid renderer in blocks where the player may enter the fluid (e.g. smeltery fluid). */
  @SuppressWarnings("deprecation")
  public static final RenderType ENTITY_CUTOUT_NO_CULL = RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS, true);
  /** Render type for translucent without cull (i.e. two-sided quads). Used for the fluid renderer in blocks where the player may enter the fluid (e.g. smeltery fluid). */
  @SuppressWarnings("deprecation")
  public static final RenderType ENTITY_TRANSLUCENT_NO_CULL = RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS, true);

  /**
   * Render type used for the structure renderer
   */
  public static final RenderType TRANSLUCENT_FULLBRIGHT = create(
    Mantle.modId + ":translucent_fullbright",
    DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, false, false,
    RenderType.CompositeState.builder()
      .setShaderState(new RenderStateShard.ShaderStateShard(MantleShaders::getBlockFullBrightShader))
      .setLightmapState(new RenderStateShard.LightmapStateShard(false))
      .setOverlayState(OVERLAY)
      .setTextureState(BLOCK_SHEET_MIPPED)
      .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
      .createCompositeState(false));
}
