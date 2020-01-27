package slimeknights.mantle.client;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraftforge.client.model.SimpleModelTransform;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.VertexTransformer;
import net.minecraftforge.common.model.TransformationHelper;

public class ModelHelper {

  public static final IModelTransform DEFAULT_ITEM_STATE;
  public static final IModelTransform DEFAULT_TOOL_STATE;
  public static final TransformationMatrix BLOCK_THIRD_PERSON_RIGHT;
  public static final TransformationMatrix BLOCK_THIRD_PERSON_LEFT;

  public static TextureAtlasSprite getTextureFromBlockstate(BlockState state) {
    return Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);
  }

  public static BakedQuad colorQuad(int color, BakedQuad quad) {
    ColorTransformer transformer = new ColorTransformer(color, quad);
    quad.pipe(transformer);
    return transformer.build();
  }

  private static TransformationMatrix get(float tx, float ty, float tz, float ax, float ay, float az, float s) {
    return new TransformationMatrix(
            new Vector3f(tx / 16, ty / 16, tz / 16),
            TransformationHelper.quatFromXYZ(new Vector3f(ax, ay, az), true),
            new Vector3f(s, s, s),
            null).blockCenterToCorner();
  }

  static {
    {
      // equals forge:default-item
      ImmutableMap.Builder<ItemCameraTransforms.TransformType, TransformationMatrix> builder = ImmutableMap.builder();
      builder.put(ItemCameraTransforms.TransformType.GROUND, get(0, 2, 0, 0, 0, 0, 0.5f));
      builder.put(ItemCameraTransforms.TransformType.HEAD, get(0, 13, 7, 0, 180, 0, 1));
      builder.put(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, get(0, 3, 1, 0, 0, 0, 0.55f));
      builder.put(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, get(0, 3, 1, 0, 0, 0, 0.55f));
      builder.put(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, get(1.13f, 3.2f, 1.13f, 0, -90, 25, 0.68f));
      builder.put(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, get(1.13f, 3.2f, 1.13f, 0, 90, -25, 0.68f));
      DEFAULT_ITEM_STATE = new SimpleModelTransform(builder.build());
    }
    {
      // equals forge:default-tool
      ImmutableMap.Builder<ItemCameraTransforms.TransformType, TransformationMatrix> builder = ImmutableMap.builder();
      builder.put(ItemCameraTransforms.TransformType.GROUND, get(0, 2, 0, 0, 0, 0, 0.5f));
      builder.put(ItemCameraTransforms.TransformType.HEAD, get(0, 13, 7, 0, 180, 0, 1));
      builder.put(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, get(0, 4, 0.5f, 0, -90, 55, 0.85f));
      builder.put(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, get(0, 4, 0.5f, 0, 90, -55, 0.85f));
      builder.put(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, get(1.13f, 3.2f, 1.13f, 0, -90, 25, 0.68f));
      builder.put(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, get(1.13f, 3.2f, 1.13f, 0, 90, -25, 0.68f));
      DEFAULT_TOOL_STATE = new SimpleModelTransform(builder.build());

    }
    {
      BLOCK_THIRD_PERSON_RIGHT = get(0, 2.5f, 0, 75, 45, 0, 0.375f);
      BLOCK_THIRD_PERSON_LEFT = get(0, 0, 0, 0, 255, 0, 0.4f);
    }
  }

  private static class ColorTransformer extends VertexTransformer {

    private final float r, g, b, a;

    public ColorTransformer(int color, BakedQuad quad) {
      super(new BakedQuadBuilder(quad.func_187508_a()));

      int a = (color >> 24);
      if (a == 0) {
        a = 255;
      }
      int r = (color >> 16) & 0xFF;
      int g = (color >> 8) & 0xFF;
      int b = (color >> 0) & 0xFF;

      this.r = (float) r / 255f;
      this.g = (float) g / 255f;
      this.b = (float) b / 255f;
      this.a = (float) a / 255f;
    }

    @Override
    public void put(int element, float... data) {
      VertexFormatElement.Usage usage = this.parent.getVertexFormat().getElements().get(element).getUsage();

      // transform normals and position
      if (usage == VertexFormatElement.Usage.COLOR && data.length >= 4) {
        data[0] = this.r;
        data[1] = this.g;
        data[2] = this.b;
        data[3] = this.a;
      }
      super.put(element, data);
    }

    public BakedQuad build() {
      return ((BakedQuadBuilder) this.parent).build();
    }
  }
}
