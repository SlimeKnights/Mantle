package slimeknights.mantle.client;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelState;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.TRSRTransformation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Map;

import javax.vecmath.Vector3f;

public class ModelHelper {

  static final Type maptype = new TypeToken<Map<String, String>>() {}.getType();
  private static final Gson
      GSON =
      new GsonBuilder().registerTypeAdapter(maptype, ModelTextureDeserializer.INSTANCE).create();

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
    int[] data = quad.getVertexData();

    int a = (color >> 24);
    if(a == 0) {
      a = 255;
    }

    int c = 0;
    c |= ((color >> 16) & 0xFF) << 0; // red
    c |= ((color >> 8) & 0xFF) << 8; // green
    c |= ((color >> 0) & 0xFF) << 16; // blue
    c |= (a & 0xFF) << 24; // alpha

    // update color in the data. all 4 Vertices.
    for(int i = 0; i < 4; i++) {
      data[i * 7 + 3] = c;
    }

    return new BakedQuad(data, quad.getTintIndex(), quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting(), quad.getFormat());
  }

  public static Map<String, String> loadTexturesFromJson(ResourceLocation location) throws IOException {
    // get the json
    IResource
        iresource =
        Minecraft.getMinecraft().getResourceManager()
                 .getResource(new ResourceLocation(location.getResourceDomain(), location.getResourcePath() + ".json"));
    Reader reader = new InputStreamReader(iresource.getInputStream(), Charsets.UTF_8);

    return GSON.fromJson(reader, maptype);
  }

  public static ImmutableList<ResourceLocation> loadTextureListFromJson(ResourceLocation location) throws IOException {
    ImmutableList.Builder<ResourceLocation> builder = ImmutableList.builder();
    for(String s : loadTexturesFromJson(location).values()) {
      builder.add(new ResourceLocation(s));
    }

    return builder.build();
  }

  public static ResourceLocation getModelLocation(ResourceLocation location) {
    return new ResourceLocation(location.getResourceDomain(), "models/" + location.getResourcePath() + ".json");
  }

  private static TRSRTransformation get(float tx, float ty, float tz, float ax, float ay, float az, float s)
  {
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
      BLOCK_THIRD_PERSON_LEFT  = get(0, 0, 0, 0, 255, 0, 0.4f);
    }
  }

  /**
   * Deseralizes a json in the format of { "textures": { "foo": "texture",... }}
   * Ignores all invalid json
   */
  public static class ModelTextureDeserializer implements JsonDeserializer<Map<String, String>> {

    public static final ModelTextureDeserializer INSTANCE = new ModelTextureDeserializer();

    private static final Gson GSON = new Gson();

    @Override
    public Map<String, String> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      JsonObject obj = json.getAsJsonObject();
      JsonElement texElem = obj.get("textures");

      if(texElem == null) {
        throw new JsonParseException("Missing textures entry in json");
      }

      return GSON.fromJson(texElem, maptype);
    }
  }
}
