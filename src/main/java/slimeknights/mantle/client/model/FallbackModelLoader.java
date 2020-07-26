package slimeknights.mantle.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.fml.ModList;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

/**
 * Loads the first model from a list of models that has a loaded mod ID, ideal for optional CTM model support
 */
@RequiredArgsConstructor
public class FallbackModelLoader implements IModelLoader<FallbackModelLoader.BlockModelWrapper> {
  /** Loader instance */
  public static final FallbackModelLoader INSTANCE = new FallbackModelLoader();

  @Override
  public void onResourceManagerReload(IResourceManager resourceManager) {}

  @Override
  public BlockModelWrapper read(JsonDeserializationContext context, JsonObject data) {
    JsonArray models = JSONUtils.getJsonArray(data, "models");
    if (models.size() < 2) {
      throw new JsonSyntaxException("Fallback model must contain at least 2 models");
    }

    // try loading each model
    for (int i = 0; i < models.size(); i++) {
      String debugName = "models[" + i + "]";
      JsonObject entry = JSONUtils.getJsonObject(models.get(i), debugName);

      // first, determine required mod ID
      String modId = null;
      if (entry.has("fallback_mod_id")) {
        modId = JSONUtils.getString(entry, "fallback_mod_id");
      } else if (entry.has("loader")) {
        ResourceLocation loader = new ResourceLocation(JSONUtils.getString(entry, "loader"));
        modId = loader.getNamespace();
      }

      // if the mod is loaded, try loading the given model
      if (modId == null || ModList.get().isLoaded(modId)) {
        try {
          // use a model wrapper to ensure the child model gets the proper context
          // this means its not possible to extend the fallback model, but that is not normally possible with loaders
          return new BlockModelWrapper(context.deserialize(entry, BlockModel.class));
        } catch (JsonSyntaxException e) {
          // wrap exceptions to make it more clear what failed
          throw new JsonSyntaxException("Failed to parse fallback model " + debugName, e);
        }
      }
    }

    // no model was successful, sadness
    throw new JsonSyntaxException("Failed to load fallback model, all " + models.size() + " variants had a failed condition");
  }

  /**
   * Wrapper around a single block model, redirects all standard calls to vanilla logic
   * Final baked model will still be the original instance, which is what is important
   */
  @AllArgsConstructor
  static class BlockModelWrapper implements IModelGeometry<BlockModelWrapper> {
    private final BlockModel model;

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial,TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
      return model.bakeModel(bakery, model, spriteGetter, modelTransform, modelLocation, true);
    }

    @Override
    public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation,IUnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
      return model.getTextures(modelGetter, missingTextureErrors);
    }
  }
}
