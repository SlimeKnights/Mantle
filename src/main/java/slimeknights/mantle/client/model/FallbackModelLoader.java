package slimeknights.mantle.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.fml.ModList;
import slimeknights.mantle.client.model.FallbackModelLoader.BlockModelWrapper;
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
  public void apply(ResourceManager resourceManager) {}

  @Override
  public BlockModelWrapper read(JsonDeserializationContext context, JsonObject data) {
    JsonArray models = JsonHelper.getArray(data, "models");
    if (models.size() < 2) {
      throw new JsonSyntaxException("Fallback model must contain at least 2 models");
    }

    // try loading each model
    for (int i = 0; i < models.size(); i++) {
      String debugName = "models[" + i + "]";
      JsonObject entry = JsonHelper.asObject(models.get(i), debugName);

      // first, determine required mod ID
      String modId = null;
      if (entry.has("fallback_mod_id")) {
        modId = JsonHelper.getString(entry, "fallback_mod_id");
      } else if (entry.has("loader")) {
        Identifier loader = new Identifier(JsonHelper.getString(entry, "loader"));
        modId = loader.getNamespace();
      }

      // if the mod is loaded, try loading the given model
      if (modId == null || ModList.get().isLoaded(modId)) {
        try {
          // use a model wrapper to ensure the child model gets the proper context
          // this means its not possible to extend the fallback model, but that is not normally possible with loaders
          return new BlockModelWrapper(context.deserialize(entry, JsonUnbakedModel.class));
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
    private final JsonUnbakedModel model;

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelLoader bakery, Function<SpriteIdentifier,Sprite> spriteGetter, ModelBakeSettings modelTransform, ModelOverrideList overrides, Identifier modelLocation) {
      return model.bake(bakery, model, spriteGetter, modelTransform, modelLocation, true);
    }

    @Override
    public Collection<SpriteIdentifier> getTextures(IModelConfiguration owner, Function<Identifier,UnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
      return model.getTextureDependencies(modelGetter, missingTextureErrors);
    }
  }
}
