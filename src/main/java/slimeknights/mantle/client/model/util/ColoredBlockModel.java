package slimeknights.mantle.client.model.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.FaceDirection;
import net.minecraft.client.renderer.FaceDirection.VertexInformation;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.BlockFaceUV;
import net.minecraft.client.renderer.model.BlockPart;
import net.minecraft.client.renderer.model.BlockPartFace;
import net.minecraft.client.renderer.model.BlockPartRotation;
import net.minecraft.client.renderer.model.FaceBakery;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.LogicHelper;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static net.minecraft.client.renderer.model.BlockModel.FACE_BAKERY;

/**
 * Block model that supports coloring elements and setting element lighting. Similar to {@link MantleItemLayerModel} but for blocks
 */
@RequiredArgsConstructor
public class ColoredBlockModel implements IModelGeometry<ColoredBlockModel> {
  public static final Loader LOADER = new Loader();

  /** Base model to display */
  private final SimpleBlockModel model;
  /** Colors to use for each piece */
  private final List<ColorData> colorData;

  @Override
  public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation,IUnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
    return model.getTextures(owner, modelGetter, missingTextureErrors);
  }

  /**
   * Bakes a single part of the model into the builder
   * @param builder       Baked model builder
   * @param owner         Model owner
   * @param part          Part to bake
   * @param color         Color tint, use -1 for no tint
   * @param luminosity    Luminosity for fullbright, use 0 for normal lighting
   * @param transform     Model transforms
   * @param spriteGetter  Sprite getter
   * @param location      Model location
   */
  public static void bakePart(SimpleBakedModel.Builder builder, IModelConfiguration owner, BlockPart part, int color, int luminosity, IModelTransform transform, Function<RenderMaterial,TextureAtlasSprite> spriteGetter, ResourceLocation location) {
    for (Direction direction : part.mapFaces.keySet()) {
      BlockPartFace face = part.mapFaces.get(direction);
      // ensure the name is not prefixed (it always is)
      String texture = face.texture;
      if (texture.charAt(0) == '#') {
        texture = texture.substring(1);
      }
      // bake the face with the extra colors
      TextureAtlasSprite sprite = spriteGetter.apply(owner.resolveTexture(texture));
      BakedQuad quad = bakeFace(part, face, sprite, direction, transform, color, luminosity, location);
      // apply cull face
      if (face.cullFace == null) {
        builder.addGeneralQuad(quad);
      } else {
        builder.addFaceQuad(Direction.rotateFace(transform.getRotation().getMatrix(), face.cullFace), quad);
      }
    }
  }

  /**
   * Bakes a list of block part elements into a model
   * @param owner         Model configuration
   * @param elements      Model elements
   * @param transform     Model transform
   * @param overrides     Model overrides
   * @param spriteGetter  Sprite getter instance
   * @param location      Model bake location
   * @return  Baked model
   */
  public static IBakedModel bakeModel(IModelConfiguration owner, List<BlockPart> elements, List<ColorData> colorData, IModelTransform transform, ItemOverrideList overrides, Function<RenderMaterial,TextureAtlasSprite> spriteGetter, ResourceLocation location) {
    // iterate parts, adding to the builder
    TextureAtlasSprite particle = spriteGetter.apply(owner.resolveTexture("particle"));
    SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(owner, overrides).setTexture(particle);
    int size = elements.size();
    for (int i = 0; i < size; i++) {
      BlockPart part = elements.get(i);
      ColorData colors = LogicHelper.getOrDefault(colorData, i, ColorData.DEFAULT);
      bakePart(builder, owner, part, colors.getColor(), colors.getLuminosity(), transform, spriteGetter, location);
    }
    return builder.build();
  }

  @Override
  public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial,TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
    return bakeModel(owner, model.getElements(), colorData, modelTransform, overrides, spriteGetter, modelLocation);
  }

  /** Data class for setting properties when baking colored elements */
  @Data
  public static class ColorData {
    public static final ColorData DEFAULT = new ColorData(-1, -1);
    private final int color;
    private final int luminosity;

    /** Parses the color data from JSON */
    public static ColorData fromJson(JsonObject json) {
      int color = JsonHelper.parseColor(JSONUtils.getString(json, "color", ""));
      int luminosity = JSONUtils.getInt(json, "luminosity", 0);
      return new ColorData(color, luminosity);
    }
  }

  /** Loader logic */
  private static class Loader implements IModelLoader<ColoredBlockModel> {
    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {}

    @Override
    public ColoredBlockModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
      SimpleBlockModel model = SimpleBlockModel.deserialize(deserializationContext, modelContents);
      List<ColorData> colorData = JsonHelper.parseList(modelContents, "colors", ColorData::fromJson);
      return new ColoredBlockModel(model, colorData);
    }
  }


  /* Face bakery */

  /**
   * Extension of {@code BlockModel#bakeFace(BlockPart, BlockPartFace, TextureAtlasSprite, Direction, IModelTransform, ResourceLocation)} with color and luminosity arguments
   * @param part        Part containing the face
   * @param face        Face data
   * @param sprite      Sprite for the face
   * @param facing      Direction of the face
   * @param transform   Transform for the face
   * @param color       Hard tint for the part, use -1 for no tint
   * @param luminosity  Lighting for the part, 0 for no extra lighting
   * @param location    Model location for errors
   */
  public static BakedQuad bakeFace(BlockPart part, BlockPartFace face, TextureAtlasSprite sprite, Direction facing, IModelTransform transform, int color, int luminosity, ResourceLocation location) {
    return bakeQuad(part.positionFrom, part.positionTo, face, sprite, facing, transform, part.partRotation, part.shade, color, luminosity, location);
  }

  /**
   * Extension of {@link FaceBakery#bakeQuad(Vector3f, Vector3f, BlockPartFace, TextureAtlasSprite, Direction, IModelTransform, BlockPartRotation, boolean, ResourceLocation)} with color and luminosity arguments
   * @param posFrom        Face start position
   * @param posTo          Face end position
   * @param face           Face data
   * @param sprite         Sprite for the face
   * @param facing         Direction of the face
   * @param transform      Transform for the face
   * @param partRotation   Rotation for the part
   * @param shade          If true, shades the part
   * @param color          Hard tint for the part, use -1 for no tint
   * @param luminosity     Lighting for the part, 0 for no extra lighting
   * @param location       Model location for errors
   * @return  Baked quad
   */
  public static BakedQuad bakeQuad(Vector3f posFrom, Vector3f posTo, BlockPartFace face, TextureAtlasSprite sprite,
                                   Direction facing, IModelTransform transform, @Nullable BlockPartRotation partRotation,
                                   boolean shade, int color, int luminosity, ResourceLocation location) {
    BlockFaceUV faceUV = face.blockFaceUV;
    if (transform.isUvLock()) {
      faceUV = FaceBakery.updateFaceUV(face.blockFaceUV, facing, transform.getRotation(), location);
    }
    float[] originalUV = new float[faceUV.uvs.length];
    System.arraycopy(faceUV.uvs, 0, originalUV, 0, originalUV.length);
    float shrinkRatio = sprite.getUvShrinkRatio();
    float u = (faceUV.uvs[0] + faceUV.uvs[0] + faceUV.uvs[2] + faceUV.uvs[2]) / 4.0F;
    float v = (faceUV.uvs[1] + faceUV.uvs[1] + faceUV.uvs[3] + faceUV.uvs[3]) / 4.0F;
    faceUV.uvs[0] = MathHelper.lerp(shrinkRatio, faceUV.uvs[0], u);
    faceUV.uvs[2] = MathHelper.lerp(shrinkRatio, faceUV.uvs[2], u);
    faceUV.uvs[1] = MathHelper.lerp(shrinkRatio, faceUV.uvs[1], v);
    faceUV.uvs[3] = MathHelper.lerp(shrinkRatio, faceUV.uvs[3], v);
    int[] vertexData = makeQuadVertexData(faceUV, sprite, facing, FACE_BAKERY.getPositionsDiv16(posFrom, posTo), transform.getRotation(), partRotation, color, luminosity);
    Direction direction = FaceBakery.getFacingFromVertexData(vertexData);
    System.arraycopy(originalUV, 0, faceUV.uvs, 0, originalUV.length);
    if (partRotation == null) {
      FACE_BAKERY.applyFacing(vertexData, direction);
    }
    ForgeHooksClient.fillNormal(vertexData, direction);
    return new BakedQuad(vertexData, face.tintIndex, direction, sprite, shade);
  }

  /** Clone of the vanilla method with 2 extra parameters */
  private static int[] makeQuadVertexData(BlockFaceUV uvs, TextureAtlasSprite sprite, Direction orientation, float[] posDiv16, TransformationMatrix rotationIn, @Nullable BlockPartRotation partRotation, int color, int luminosity) {
    int[] vertexData = new int[32];
    for(int i = 0; i < 4; ++i) {
      fillVertexData(vertexData, i, orientation, uvs, posDiv16, sprite, rotationIn, partRotation, color, luminosity);
    }
    return vertexData;
  }

  /** Clone of the vanilla method with 2 extra parameters */
  private static void fillVertexData(int[] vertexData, int vertexIndex, Direction facing, BlockFaceUV blockFaceUVIn, float[] posDiv16, TextureAtlasSprite sprite, TransformationMatrix rotationIn, @Nullable BlockPartRotation partRotation, int color, int luminosity) {
    VertexInformation vertexInfo = FaceDirection.getFacing(facing).getVertexInformation(vertexIndex);
    Vector3f vector3f = new Vector3f(posDiv16[vertexInfo.xIndex], posDiv16[vertexInfo.yIndex], posDiv16[vertexInfo.zIndex]);
    FACE_BAKERY.rotatePart(vector3f, partRotation);
    FACE_BAKERY.rotateVertex(vector3f, rotationIn);
    fillVertexData(vertexData, vertexIndex, vector3f, sprite, blockFaceUVIn, color, luminosity);
  }

  /** Clone of the vanilla method with 2 extra parameters, major logic changes are in this code */
  private static void fillVertexData(int[] vertexData, int vertexIndex, Vector3f vector, TextureAtlasSprite sprite, BlockFaceUV blockFaceUV, int color, int luminosity) {
    int i = vertexIndex * 8;
    // XYZ - 3 ints
    vertexData[i] = Float.floatToRawIntBits(vector.getX());
    vertexData[i + 1] = Float.floatToRawIntBits(vector.getY());
    vertexData[i + 2] = Float.floatToRawIntBits(vector.getZ());
    // color - 1 int. vanilla uses -1 here
    vertexData[i + 3] = color;
    // UV - 2 ints
    vertexData[i + 4] = Float.floatToRawIntBits(sprite.getInterpolatedU((double)blockFaceUV.getVertexU(vertexIndex) * .999 + blockFaceUV.getVertexU((vertexIndex + 2) % 4) * .001));
    vertexData[i + 5] = Float.floatToRawIntBits(sprite.getInterpolatedV((double)blockFaceUV.getVertexV(vertexIndex) * .999 + blockFaceUV.getVertexV((vertexIndex + 2) % 4) * .001));
    // light UV - 1 ints, just setting block light here rather than block and sky. vanilla uses 0 here
    vertexData[i + 6] = (luminosity << 4);
  }
}
