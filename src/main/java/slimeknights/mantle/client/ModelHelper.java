package slimeknights.mantle.client;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.client.model.pipeline.VertexTransformer;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class ModelHelper {

  public static final Optional<IModelState> DEFAULT_ITEM_STATE;
  public static final Optional<IModelState> DEFAULT_TOOL_STATE;
  public static final TRSRTransformation BLOCK_THIRD_PERSON_RIGHT;
  public static final TRSRTransformation BLOCK_THIRD_PERSON_LEFT;

  public static TextureAtlasSprite getTextureFromBlock(Block block, int meta) {
    IBlockState state = block.getStateFromMeta(meta);
    return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);
  }

  public static TextureAtlasSprite getTextureFromBlockstate(IBlockState state) {
    return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);
  }

  public static BakedQuad colorQuad(int color, BakedQuad quad) {
    ColorTransformer transformer = new ColorTransformer(color, quad.getFormat());
    quad.pipe(transformer);
    return transformer.build();
  }

  private static TRSRTransformation get(float tx, float ty, float tz, float ax, float ay, float az, float s) {
    return TRSRTransformation.blockCenterToCorner(new TRSRTransformation(
        new Vector3f(tx / 16, ty / 16, tz / 16),
        TRSRTransformation.quatFromXYZDegrees(new Vector3f(ax, ay, az)),
        new Vector3f(s, s, s),
        null));
  }

  static {
    {
      // equals forge:default-item
      DEFAULT_ITEM_STATE = Optional.<IModelState>of(new SimpleModelState(ImmutableMap.of(
          ItemCameraTransforms.TransformType.GROUND, get(0, 2, 0, 0, 0, 0, 0.5f),
          ItemCameraTransforms.TransformType.HEAD, get(0, 13, 7, 0, 180, 0, 1),
          ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, get(0, 3, 1, 0, 0, 0, 0.55f),
          ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, get(1.13f, 3.2f, 1.13f, 0, -90, 25, 0.68f))));
    }
    {
      // equals forge:default-tool
      DEFAULT_TOOL_STATE = Optional.<IModelState>of(new SimpleModelState(ImmutableMap.of(
          ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, get(0, 4, 0.5f, 0, -90, 55, 0.85f),
          ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, get(0, 4, 0.5f, 0, 90, -55, 0.85f),
          ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, get(1.13f, 3.2f, 1.13f, 0, -90, 25, 0.68f),
          ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, get(1.13f, 3.2f, 1.13f, 0, 90, -25, 0.68f))));
    }
    {
      BLOCK_THIRD_PERSON_RIGHT = get(0, 2.5f, 0, 75, 45, 0, 0.375f);
      BLOCK_THIRD_PERSON_LEFT = get(0, 0, 0, 0, 255, 0, 0.4f);
    }
  }


  private static class ColorTransformer extends VertexTransformer {

    private final float r,g,b,a;

    public ColorTransformer(int color, VertexFormat format) {
      super(new UnpackedBakedQuad.Builder(format));

      int a = (color >> 24);
      if(a == 0) {
        a = 255;
      }
      int r = (color >> 16) & 0xFF;
      int g = (color >> 8) & 0xFF;
      int b = (color >> 0) & 0xFF;

      this.r = (float)r/255f;
      this.g = (float)g/255f;
      this.b = (float)b/255f;
      this.a = (float)a/255f;
    }

    @Override
    public void put(int element, float... data) {
      VertexFormatElement.EnumUsage usage = parent.getVertexFormat().getElement(element).getUsage();

      // transform normals and position
      if(usage == VertexFormatElement.EnumUsage.COLOR && data.length >= 4) {
        data[0] = r;
        data[1] = g;
        data[2] = b;
        data[3] = a;
      }
      super.put(element, data);
    }

    public UnpackedBakedQuad build() {
      return ((UnpackedBakedQuad.Builder) parent).build();
    }
  }
}
