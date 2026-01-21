package slimeknights.mantle.client.render;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import slimeknights.mantle.Mantle;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.ELEMENT_COLOR;
import static com.mojang.blaze3d.vertex.DefaultVertexFormat.ELEMENT_NORMAL;
import static com.mojang.blaze3d.vertex.DefaultVertexFormat.ELEMENT_PADDING;
import static com.mojang.blaze3d.vertex.DefaultVertexFormat.ELEMENT_POSITION;
import static com.mojang.blaze3d.vertex.DefaultVertexFormat.ELEMENT_UV0;
import static com.mojang.blaze3d.vertex.DefaultVertexFormat.ELEMENT_UV1;
import static com.mojang.blaze3d.vertex.DefaultVertexFormat.ELEMENT_UV2;

/**
 * Class for render types defined by Mantle
 */
public class MantleRenderTypes extends RenderType {

  private MantleRenderTypes(String name, VertexFormat format, Mode mode, int bufferSize, boolean useDelegate, boolean needsSorting, Runnable setupTaskIn, Runnable clearTaskIn) {
    super(name, format, mode, bufferSize, useDelegate, needsSorting, setupTaskIn, clearTaskIn);
  }

  /** Extension of {@link RenderType#POSITION_COLOR_TEX_LIGHTMAP_SHADER} with fog information based on {@link RenderType#ENTITY_TRANSLUCENT_CULL} */
  public static final RenderStateShard.ShaderStateShard FLUID_SHADER = new RenderStateShard.ShaderStateShard(MantleShaders::getConfiguredFluidShader);

  /**
   * Render type used for the fluid renderer.
   * TODO 1.21: can we replace this with {@link RenderType#ENTITY_TRANSLUCENT_CULL}? Would require including normals in our vertex format.
   */
  public static final RenderType FLUID = create(
    Mantle.modId + ":block_render_type",
    DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true,
    RenderType.CompositeState.builder()
      .setLightmapState(LIGHTMAP)
      .setShaderState(FLUID_SHADER)
      .setTextureState(BLOCK_SHEET_MIPPED)
      .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
      .createCompositeState(false));

  /**
   * Render type used for the structure renderer
   */
  public static final VertexFormat BLOCK_WITH_OVERLAY = new VertexFormat(ImmutableMap.of(
    "Position", ELEMENT_POSITION,
    "Color", ELEMENT_COLOR,
    "UV0", ELEMENT_UV0,
    "UV1", ELEMENT_UV1,
    "UV2", ELEMENT_UV2,
    "Normal", ELEMENT_NORMAL,
    "Padding", ELEMENT_PADDING));

  public static final RenderType TRANSLUCENT_FULLBRIGHT = create(
    Mantle.modId + ":translucent_fullbright",
    BLOCK_WITH_OVERLAY, Mode.QUADS, 256, false, false,
    RenderType.CompositeState.builder()
      .setShaderState(new RenderStateShard.ShaderStateShard(MantleShaders::getBlockFullBrightShader))
      .setLightmapState(new RenderStateShard.LightmapStateShard(false))
      .setOverlayState(OVERLAY)
      .setTextureState(BLOCK_SHEET_MIPPED)
      .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
      .createCompositeState(false));
}
