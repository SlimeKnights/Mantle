package slimeknights.mantle.client.model;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import slimeknights.mantle.client.model.util.DynamicBakedWrapper;
import slimeknights.mantle.client.model.util.ModelConfigurationWrapper;
import slimeknights.mantle.client.model.util.ModelHelper;
import slimeknights.mantle.client.model.util.ModelTextureIteratable;
import slimeknights.mantle.client.model.util.SimpleBlockModel;
import slimeknights.mantle.item.RetexturedBlockItem;
import slimeknights.mantle.util.RetexturedHelper;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

/**
 * Model that dynamically retextures a list of textures based on data from {@link RetexturedHelper}.
 */
@SuppressWarnings("WeakerAccess")
public class RetexturedModel implements IModelGeometry<RetexturedModel> {
  private final SimpleBlockModel model;
  private final Set<String> retextured;

  protected RetexturedModel(SimpleBlockModel model, Set<String> retextured) {
    this.model = model;
    this.retextured = retextured;
  }

  @Override
  public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation,IUnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
    return model.getTextures(owner, modelGetter, missingTextureErrors);
  }

  @Override
  public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial,TextureAtlasSprite> spriteGetter, IModelTransform transform, ItemOverrideList overrides, ResourceLocation location) {
    // bake the model and return
    IBakedModel baked = model.bakeModel(owner, transform, overrides, spriteGetter, location);
    return new BakedModel(baked, owner, model, transform, getAllRetextured(owner, this.model, retextured));
  }

  /**
   * Gets a list of all names to retexture based on the block model texture references
   * @param owner        Model config instance
   * @param model        Model fallback
   * @param originalSet  Original list of names to retexture
   * @return  Set of textures including parent textures
   */
  public static Set<String> getAllRetextured(IModelConfiguration owner, SimpleBlockModel model, Set<String> originalSet) {
    Set<String> retextured = Sets.newHashSet(originalSet);
    for (Map<String,Either<RenderMaterial, String>> textures : ModelTextureIteratable.of(owner, model)) {
      textures.forEach((name, either) ->
        either.ifRight(parent -> {
          if (retextured.contains(parent)) {
            retextured.add(name);
          }
        })
      );
    }
    return ImmutableSet.copyOf(retextured);
  }


  /** Registered model loader instance registered */
  public static class Loader implements IModelLoader<RetexturedModel> {
    public static final Loader INSTANCE = new Loader();
    private Loader() {}

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {}

    @Override
    public RetexturedModel read(JsonDeserializationContext context, JsonObject json) {
      // get base model
      SimpleBlockModel model = SimpleBlockModel.deserialize(context, json);

      // get list of textures to retexture
      Set<String> retextured = getRetextured(json);

      // return retextured model
      return new RetexturedModel(model, retextured);
    }

    /**
     * Gets the list of retextured textures from the model
     * @param json  Model json
     * @return  List of textures
     */
    public static Set<String> getRetextured(JsonObject json) {
      if (json.has("retextured")) {
        // if an array, set from each texture in array
        JsonElement retextured = json.get("retextured");
        if (retextured.isJsonArray()) {
          JsonArray array = retextured.getAsJsonArray();
          if (array.size() == 0) {
            throw new JsonSyntaxException("Must have at least one texture in retextured");
          }
          ImmutableSet.Builder<String> builder = ImmutableSet.builder();
          for (int i = 0; i < array.size(); i++) {
            builder.add(JSONUtils.getString(array.get(i), "retextured[" + i + "]"));
          }
          return builder.build();
        }
        // if string, single texture
        if (retextured.isJsonPrimitive()) {
          return ImmutableSet.of(retextured.getAsString());
        }
      }
      // if neither or missing, error
      throw new JsonSyntaxException("Missing retextured, expected to find a String or a JsonArray");
    }
  }

  /** Baked variant of the model, used to swap out quads based on the texture */
  public static class BakedModel extends DynamicBakedWrapper<IBakedModel> {
    /** Cache of texture name to baked model */
    private final Map<ResourceLocation,IBakedModel> cache = new HashMap<>();
    /* Properties for rebaking */
    private final IModelConfiguration owner;
    private final SimpleBlockModel model;
    private final IModelTransform transform;
    /** List of texture names that are retextured */
    private final Set<String> retextured;

    protected BakedModel(IBakedModel baked, IModelConfiguration owner, SimpleBlockModel model, IModelTransform transform, Set<String> retextured) {
      super(baked);
      this.model = model;
      this.owner = owner;
      this.transform = transform;
      this.retextured = retextured;
    }

    /**
     * Gets the model with the given texture applied
     * @param name  Texture location
     * @return  Retextured model
     */
    private IBakedModel getRetexturedModel(ResourceLocation name) {
      return model.bakeDynamic(new RetexturedConfiguration(owner, retextured, name), transform);
    }

    /**
     * Gets a cached retextured model, computing it if missing from the cache
     * @param block  Block determining the texture
     * @return  Retextured model
     */
    private IBakedModel getCachedModel(Block block) {
      return cache.computeIfAbsent(ModelHelper.getParticleTexture(block), this::getRetexturedModel);
    }

    @Override
    public TextureAtlasSprite getParticleTexture(IModelData data) {
      // if particle is retextured, fetch particle from the cached model
      if (retextured.contains("particle")) {
        Block block = data.getData(RetexturedHelper.BLOCK_PROPERTY);
        if (block != null) {
          return getCachedModel(block).getParticleTexture(data);
        }
      }
      return originalModel.getParticleTexture(data);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, Random random, IModelData data) {
      Block block = data.getData(RetexturedHelper.BLOCK_PROPERTY);
      if (block == null) {
        return originalModel.getQuads(state, direction, random, data);
      }
      return getCachedModel(block).getQuads(state, direction, random, data);
    }

    @Override
    public ItemOverrideList getOverrides() {
      return RetexturedOverride.INSTANCE;
    }
  }

  /**
   * Model configuration wrapper to retexture the block
   */
  public static class RetexturedConfiguration extends ModelConfigurationWrapper {
    /** List of textures to retexture */
    private final Set<String> retextured;
    /** Replacement texture */
    private final RenderMaterial texture;

    /**
     * Creates a new configuration wrapper
     * @param base        Original model configuration
     * @param retextured  Set of textures that should be retextured
     * @param texture     New texture to replace those in the set
     */
    public RetexturedConfiguration(IModelConfiguration base, Set<String> retextured, ResourceLocation texture) {
      super(base);
      this.retextured = retextured;
      this.texture = ModelLoaderRegistry.blockMaterial(texture);
    }

    @Override
    public boolean isTexturePresent(String name) {
      if (retextured.contains(name)) {
        return !MissingTextureSprite.getLocation().equals(texture.getTextureLocation());
      }
      return super.isTexturePresent(name);
    }

    @Override
    public RenderMaterial resolveTexture(String name) {
      if (retextured.contains(name)) {
        return texture;
      }
      return super.resolveTexture(name);
    }
  }

  /** Override list to swap the texture in from NBT */
  private static class RetexturedOverride extends ItemOverrideList {
    private static final RetexturedOverride INSTANCE = new RetexturedOverride();

    @Nullable
    @Override
    public IBakedModel func_239290_a_(IBakedModel originalModel, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity) {
      if (stack.isEmpty() || !stack.hasTag()) {
        return originalModel;
      }

      // get the block first, ensuring its valid
      Block block = RetexturedBlockItem.getTexture(stack);
      if (block == Blocks.AIR) {
        return originalModel;
      }

      // if valid, use the block
      return ((BakedModel)originalModel).getCachedModel(block);
    }
  }
}
