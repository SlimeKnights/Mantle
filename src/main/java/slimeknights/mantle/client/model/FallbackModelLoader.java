package slimeknights.mantle.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.fml.ModList;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

/**
 * Loads the first model from a list of models that has a loaded mod ID, ideal for optional CTM model support
 */
@RequiredArgsConstructor
public enum FallbackModelLoader implements IGeometryLoader<FallbackModelLoader.BlockModelWrapper> {
  INSTANCE;

  @Override
  public BlockModelWrapper read(JsonObject data, JsonDeserializationContext context) {
    JsonArray models = GsonHelper.getAsJsonArray(data, "models");
    if (models.size() < 2) {
      throw new JsonSyntaxException("Fallback model must contain at least 2 models");
    }

    // try loading each model
    for (int i = 0; i < models.size(); i++) {
      String debugName = "models[" + i + "]";
      JsonObject entry = GsonHelper.convertToJsonObject(models.get(i), debugName);

      // first, determine required mod ID
      String modId = null;
      if (entry.has("fallback_mod_id")) {
        modId = GsonHelper.getAsString(entry, "fallback_mod_id");
      } else if (entry.has("loader")) {
        ResourceLocation loader = new ResourceLocation(GsonHelper.getAsString(entry, "loader"));
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
  record BlockModelWrapper(BlockModel model) implements IUnbakedGeometry<BlockModelWrapper> {
    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function<Material,TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
      return model.bake(bakery, model, spriteGetter, modelTransform, modelLocation, true);
    }

    @Override
    public Collection<Material> getMaterials(IGeometryBakingContext owner, Function<ResourceLocation,UnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
      return model.getMaterials(modelGetter, missingTextureErrors);
    }
  }
}
