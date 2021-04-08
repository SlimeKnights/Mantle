package slimeknights.mantle.client.model.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.MultipartBakedModel;
import net.minecraft.client.render.model.WeightedBakedModel;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.ItemConvertible;
import net.minecraft.resource.ResourceReloadListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Utilities to help in custom models
 */
public class ModelHelper {
  private static final Map<Block,Identifier> TEXTURE_NAME_CACHE = new HashMap<>();
  /** Listener instance to clear cache */
  public static final ResourceReloadListener LISTENER = (synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor) -> {
    TEXTURE_NAME_CACHE.clear();
    return CompletableFuture.completedFuture(null);
  };

  /* Baked models */

  /**
   * Gets the model for the given block
   * @param state  Block state
   * @param clazz  Class type to cast result into
   * @param <T>    Class type
   * @return  Block model, or null if its missing or the wrong class type
   */
  @Nullable
  public static <T extends BakedModel> T getBakedModel(BlockState state, Class<T> clazz) {
    BakedModel baked = MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(state);
    // map multipart and weighted random into the first variant
    if (baked instanceof MultipartBakedModel) {
      baked = ((MultipartBakedModel)baked).components.get(0).getRight();
    }
    if (baked instanceof WeightedBakedModel) {
      baked = ((WeightedBakedModel) baked).defaultModel;
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
  public static <T extends BakedModel> T getBakedModel(ItemConvertible item, Class<T> clazz) {
    BakedModel baked = MinecraftClient.getInstance().getItemRenderer().getModels().getModel(item.asItem());
    if (clazz.isInstance(baked)) {
      return clazz.cast(baked);
    }
    return null;
  }

  /**
   * Gets the texture name for a block from the model manager
   * @param block  Block to fetch
   * @return Texture name for the block
   */
  @SuppressWarnings("deprecation")
  private static Identifier getParticleTextureInternal(Block block) {
    return MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(block.getDefaultState()).getSprite().getId();
  }

  /**
   * Gets the name of a particle texture for a block, using the cached value if present
   * @param block Block to fetch
   * @return Texture name for the block
   */
  public static Identifier getParticleTexture(Block block) {
    return TEXTURE_NAME_CACHE.computeIfAbsent(block, ModelHelper::getParticleTextureInternal);
  }

  /* JSON */

  /**
   * Converts a JSON float array to the specified object
   * @param json    JSON object
   * @param name    Name of the array in the object to fetch
   * @param size    Expected array size
   * @param mapper  Functon to map from the array to the output type
   * @param <T> Output type
   * @return  Vector3f of data
   * @throws JsonParseException  If there is no array or the length is wrong
   */
  public static <T> T arrayToObject(JsonObject json, String name, int size, Function<float[], T> mapper) {
    JsonArray array = JsonHelper.getArray(json, name);
    if (array.size() != size) {
      throw new JsonParseException("Expected " + size + " " + name + " values, found: " + array.size());
    }
    float[] vec = new float[size];
    for(int i = 0; i < size; ++i) {
      vec[i] = JsonHelper.asFloat(array.get(i), name + "[" + i + "]");
    }
    return mapper.apply(vec);
  }

  /**
   * Converts a JSON array with 3 elements into a Vector3f
   * @param json  JSON object
   * @param name  Name of the array in the object to fetch
   * @return  Vector3f of data
   * @throws JsonParseException  If there is no array or the length is wrong
   */
  public static Vector3f arrayToVector(JsonObject json, String name) {
    return arrayToObject(json, name, 3, arr -> new Vector3f(arr[0], arr[1], arr[2]));
  }

  /**
   * Gets a rotation from JSON
   * @param json  JSON parent
   * @return  Integer of 0, 90, 180, or 270
   */
  public static int getRotation(JsonObject json, String key) {
    int i = JsonHelper.getInt(json, key, 0);
    if (i >= 0 && i % 90 == 0 && i / 90 <= 3) {
      return i;
    } else {
      throw new JsonParseException("Invalid '" + key + "' " + i + " found, only 0/90/180/270 allowed");
    }
  }
}