package slimeknights.mantle.client.model.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.MultipartBakedModel;
import net.minecraft.client.renderer.model.WeightedBakedModel;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.VertexTransformer;

import javax.annotation.Nullable;

/**
 * Utilities to help in custom models
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ModelHelper {
  /* Baked models */

  /**
   * Gets the model for the given block
   * @param state  Block state
   * @param clazz  Class type to cast result into
   * @param <T>    Class type
   * @return  Block model, or null if its missing or the wrong class type
   */
  @Nullable
  public static <T extends IBakedModel> T getBakedModel(BlockState state, Class<T> clazz) {
    IBakedModel baked = Minecraft.getInstance().getModelManager().getBlockModelShapes().getModel(state);
    // map multipart and weighted random into the first variant
    if (baked instanceof MultipartBakedModel) {
      baked = ((MultipartBakedModel)baked).selectors.get(0).getRight();
    }
    if (baked instanceof WeightedBakedModel) {
      baked = ((WeightedBakedModel) baked).baseModel;
    }
    // final model should match the desired type
    if (clazz.isInstance(baked)) {
      return clazz.cast(baked);
    }
    return null;
  }

  /**
   * Gets the model for the given item
   * @param item   Item provider
   * @param clazz  Class type to cast result into
   * @param <T>    Class type
   * @return  Item model, or null if its missing or the wrong class type
   */
  @Nullable
  public static <T extends IBakedModel> T getBakedModel(IItemProvider item, Class<T> clazz) {
    IBakedModel baked = Minecraft.getInstance().getItemRenderer().getItemModelMesher().getItemModel(item.asItem());
    if (clazz.isInstance(baked)) {
      return clazz.cast(baked);
    }
    return null;
  }

  /* JSON */

  /**
   * Converts a JSON array with 3 elements into a Vector3f
   * @param json  JSON object
   * @param name  Name of the array in the object to fetch
   * @return  Vector3f of data
   * @throws JsonParseException  If there is no array or the length is wrong
   */
  public static Vector3f arrayToVector(JsonObject json, String name) {
    JsonArray array = JSONUtils.getJsonArray(json, name);
    if (array.size() != 3) {
      throw new JsonParseException("Expected 3 " + name + " values, found: " + array.size());
    }
    float[] vec = new float[3];
    for(int i = 0; i < vec.length; ++i) {
      vec[i] = JSONUtils.getFloat(array.get(i), name + "[" + i + "]");
    }

    return new Vector3f(vec[0], vec[1], vec[2]);
  }

  /**
   * Gets a rotation from JSON
   * @param json  JSON parent
   * @return  Integer of 0, 90, 180, or 270
   */
  public static int getRotation(JsonObject json, String key) {
    int i = JSONUtils.getInt(json, key, 0);
    if (i >= 0 && i % 90 == 0 && i / 90 <= 3) {
      return i;
    } else {
      throw new JsonParseException("Invalid '" + key + "' " + i + " found, only 0/90/180/270 allowed");
    }
  }

  public static BakedQuad colorQuad(int color, BakedQuad quad) {
    ColorTransformer transformer = new ColorTransformer(color, quad);
    quad.pipe(transformer);
    return transformer.build();
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
