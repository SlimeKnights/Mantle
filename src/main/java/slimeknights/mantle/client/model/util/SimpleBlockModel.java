package slimeknights.mantle.client.model.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.BlockPart;
import net.minecraft.client.renderer.model.BlockPartFace;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import slimeknights.mantle.Mantle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Simplier version of {@link BlockModel} for use in an {@link net.minecraftforge.client.model.IModelLoader}, as the owner handles most block model properties
 */
@SuppressWarnings("WeakerAccess")
public class SimpleBlockModel implements IModelGeometry<SimpleBlockModel> {
  /** Model loader for vanilla block model, mainly intended for use in fallback registration */
  public static final Loader LOADER = new Loader();
  /** Location used for baking dynamic models, name does not matter so just using a constant */
  private static final ResourceLocation BAKE_LOCATION = Mantle.getResource("dynamic_model_baking");

  /** Parent model location, used to fetch parts and for textures if the owner is not a block model */
  @Getter
  @Nullable
  private ResourceLocation parentLocation;
  /** Model parts for baked model, if empty uses parent parts */
  private final List<BlockPart> parts;
  /** Fallback textures in case the owner does not contain a block model */
  @Getter
  private final Map<String,Either<RenderMaterial, String>> textures;
  @Getter
  private BlockModel parent;

  /**
   * Creates a new simple block model
   * @param parentLocation  Location of the parent model, if unset has no parent
   * @param textures        List of textures for iteration, in case the owner is not BlockModel
   * @param parts           List of parts in the model
   */
  public SimpleBlockModel(@Nullable ResourceLocation parentLocation, Map<String,Either<RenderMaterial,String>> textures, List<BlockPart> parts) {
    this.parts = parts;
    this.textures = textures;
    this.parentLocation = parentLocation;
  }


  /* Properties */

  /**
   * Gets the elements in this simple block model
   * @return  Elements in the model
   */
  @SuppressWarnings("deprecation")
  public List<BlockPart> getElements() {
    return parts.isEmpty() && parent != null ? parent.getElements() : parts;
  }

  /* Textures */

  /**
   * Fetches parent models for this model and its parents
   * @param modelGetter  Model getter function
   */
  public void fetchParent(IModelConfiguration owner, Function<ResourceLocation,IUnbakedModel> modelGetter) {
    // no work if no parent or the parent is fetched already
    if (parent != null || parentLocation == null) {
      return;
    }

    // iterate through model parents
    Set<IUnbakedModel> chain = Sets.newLinkedHashSet();

    // load the first model directly
    parent = getParent(modelGetter, chain, parentLocation, owner.getModelName());
    // null means no model, so set missing
    if (parent == null) {
      parent = getMissing(modelGetter);
      parentLocation = ModelBakery.MODEL_MISSING;
    }

    // loop through each parent, adding in parents
    for (BlockModel link = parent; link.parentLocation != null && link.parent == null; link = link.parent) {
      chain.add(link);

      // fetch model parent
      link.parent = getParent(modelGetter, chain, link.parentLocation, link.name);

      // null means no model, so set missing
      if (link.parent == null) {
        link.parent = getMissing(modelGetter);
        link.parentLocation = ModelBakery.MODEL_MISSING;
      }
    }
  }

  /**
   * Gets the parent for a model
   * @param modelGetter  Model getter function
   * @param chain        Chain of models that are in progress
   * @param location     Location to fetch
   * @param name         Name of the model being fetched
   * @return  Block model instance, null if there was an error
   */
  @Nullable
  private static BlockModel getParent(Function<ResourceLocation,IUnbakedModel> modelGetter, Set<IUnbakedModel> chain, ResourceLocation location, String name) {
    // model must exist
    IUnbakedModel unbaked = modelGetter.apply(location);
    if (unbaked == null) {
      Mantle.logger.warn("No parent '{}' while loading model '{}'", location, name);
      return null;
    }
    // no loops in chain
    if (chain.contains(unbaked)) {
      Mantle.logger.warn("Found 'parent' loop while loading model '{}' in chain: {} -> {}", name, chain.stream().map(Object::toString).collect(Collectors.joining(" -> ")), location);
      return null;
    }
    // model must be block model, this is a serious error in vanilla
    if (!(unbaked instanceof BlockModel)) {
      throw new IllegalStateException("BlockModel parent has to be a block model.");
    }
    return (BlockModel) unbaked;
  }

  /**
   * Gets the missing model, ensuring its the right type
   * @param modelGetter  Model getter function
   * @return  Missing model as a {@link BlockModel}
   */
  @Nonnull
  private static BlockModel getMissing(Function<ResourceLocation,IUnbakedModel> modelGetter) {
    IUnbakedModel model = modelGetter.apply(ModelBakery.MODEL_MISSING);
    if (!(model instanceof BlockModel)) {
      throw new IllegalStateException("Failed to load missing model");
    }
    return (BlockModel) model;
  }

  /**
   * Gets the texture dependencies for a list of elements, allows calling outside a simple block model
   * @param owner                 Model configuration
   * @param elements              List of elements to check for textures
   * @param missingTextureErrors  Missing texture set
   * @return  Textures dependencies
   */
  public static Collection<RenderMaterial> getTextures(IModelConfiguration owner, List<BlockPart> elements, Set<Pair<String,String>> missingTextureErrors) {
    // always need a particle texture
    Set<RenderMaterial> textures = Sets.newHashSet(owner.resolveTexture("particle"));
    // iterate all elements, fetching needed textures from the material
    for(BlockPart part : elements) {
      for(BlockPartFace face : part.mapFaces.values()) {
        RenderMaterial material = owner.resolveTexture(face.texture);
        if (Objects.equals(material.getTextureLocation(), MissingTextureSprite.getLocation())) {
          missingTextureErrors.add(Pair.of(face.texture, owner.getModelName()));
        }
        textures.add(material);
      }
    }
    return textures;
  }

  /**
   * Gets the texture and model dependencies for a block model
   * @param owner                 Model configuration
   * @param modelGetter           Model getter to fetch parent models
   * @param missingTextureErrors  Missing texture set
   * @return  Textures dependencies
   */
  @Override
  public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation,IUnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
    this.fetchParent(owner, modelGetter);
    return getTextures(owner, getElements(), missingTextureErrors);
  }


  /* Baking */

  /**
   * Bakes a single part of the model into the builder
   * @param builder       Baked model builder
   * @param owner         Model owner
   * @param part          Part to bake
   * @param transform     Model transforms
   * @param spriteGetter  Sprite getter
   * @param location      Model location
   */
  public static void bakePart(SimpleBakedModel.Builder builder, IModelConfiguration owner, BlockPart part, IModelTransform transform, Function<RenderMaterial,TextureAtlasSprite> spriteGetter, ResourceLocation location) {
    for(Direction direction : part.mapFaces.keySet()) {
      BlockPartFace face = part.mapFaces.get(direction);
      // ensure the name is not prefixed (it always is)
      String texture = face.texture;
      if (texture.charAt(0) == '#') {
        texture = texture.substring(1);
      }
      // bake the face
      TextureAtlasSprite sprite = spriteGetter.apply(owner.resolveTexture(texture));
      BakedQuad bakedQuad = BlockModel.bakeFace(part, face, sprite, direction, transform, location);
      // apply cull face
      if (face.cullFace == null) {
        builder.addGeneralQuad(bakedQuad);
      } else {
        builder.addFaceQuad(Direction.rotateFace(transform.getRotation().getMatrix(), face.cullFace), bakedQuad);
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
  public static IBakedModel bakeModel(IModelConfiguration owner, List<BlockPart> elements, IModelTransform transform, ItemOverrideList overrides, Function<RenderMaterial,TextureAtlasSprite> spriteGetter, ResourceLocation location) {
    // iterate parts, adding to the builder
    TextureAtlasSprite particle = spriteGetter.apply(owner.resolveTexture("particle"));
    SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(owner, overrides).setTexture(particle);
    for(BlockPart part : elements) {
      bakePart(builder, owner, part, transform, spriteGetter, location);
    }
    return builder.build();
  }

  /**
   * Same as {@link #bakeModel(IModelConfiguration, List, IModelTransform, ItemOverrideList, Function, ResourceLocation)}, but passes in sensible defaults for values unneeded in dynamic models
   * @param owner      Model configuration
   * @param elements   Elements to bake
   * @param transform  Model transform
   * @return Baked model
   */
  public static IBakedModel bakeDynamic(IModelConfiguration owner, List<BlockPart> elements, IModelTransform transform) {
    return bakeModel(owner, elements, transform, ItemOverrideList.EMPTY, ModelLoader.defaultTextureGetter(), BAKE_LOCATION);
  }

  /**
   * Bakes the given block model
   * @param owner         Model configuration
   * @param transform     Transform to apply
   * @param overrides     Item overrides in baking
   * @param spriteGetter  Sprite getter instance
   * @param location      Bake location
   * @return  Baked model
   */
  public IBakedModel bakeModel(IModelConfiguration owner, IModelTransform transform, ItemOverrideList overrides, Function<RenderMaterial,TextureAtlasSprite> spriteGetter, ResourceLocation location) {
    return bakeModel(owner, this.getElements(), transform, overrides, spriteGetter, location);
  }

  @Override
  public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial,TextureAtlasSprite> spriteGetter, IModelTransform transform, ItemOverrideList overrides, ResourceLocation location) {
    return bakeModel(owner, transform, overrides, spriteGetter, location);
  }

  /**
   * Same as {@link #bakeModel(IModelConfiguration, IModelTransform, ItemOverrideList, Function, ResourceLocation)}, but passes in sensible defaults for values unneeded in dynamic models
   * @param owner         Model configuration
   * @param transform     Transform to apply
   * @return  Baked model
   */
  public IBakedModel bakeDynamic(IModelConfiguration owner, IModelTransform transform) {
    return bakeDynamic(owner, this.getElements(), transform);
  }


  /* Deserializing */

  /**
   * Deserializes a SimpleBlockModel from JSON
   * @param context  Json Context
   * @param json     Json element containing the model
   * @return  Serialized JSON
   */
  public static SimpleBlockModel deserialize(JsonDeserializationContext context, JsonObject json) {
    // parent, null if missing
    String parentName = JSONUtils.getString(json, "parent", "");
    ResourceLocation parent = parentName.isEmpty() ? null : new ResourceLocation(parentName);

    // textures, empty map if missing
    Map<String, Either<RenderMaterial, String>> textureMap;
    if (json.has("textures")) {
      ImmutableMap.Builder<String, Either<RenderMaterial, String>> builder = new ImmutableMap.Builder<>();
      ResourceLocation atlas = PlayerContainer.LOCATION_BLOCKS_TEXTURE;
      JsonObject textures = JSONUtils.getJsonObject(json, "textures");
      for(Entry<String, JsonElement> entry : textures.entrySet()) {
        builder.put(entry.getKey(), BlockModel.Deserializer.findTexture(atlas, entry.getValue().getAsString()));
      }
      textureMap = builder.build();
    } else {
      textureMap = Collections.emptyMap();
    }

    // elements, empty list if missing
    List<BlockPart> parts;
    if (json.has("elements")) {
      parts = getModelElements(context, JSONUtils.getJsonArray(json, "elements"), "elements");
    } else {
      parts = Collections.emptyList();
    }
    return new SimpleBlockModel(parent, textureMap, parts);
  }

  /**
   * Gets a list of models from a JSON array
   * @param context  Json Context
   * @param array    Json array
   * @return  Model list
   */
  public static List<BlockPart> getModelElements(JsonDeserializationContext context, JsonElement array, String name) {
    // if just one element, array is optional
    if (array.isJsonObject()) {
      return ImmutableList.of(context.deserialize(array.getAsJsonObject(), BlockPart.class));
    }
    // if an array, get array of elements
    if (array.isJsonArray()) {
      ImmutableList.Builder<BlockPart> builder = ImmutableList.builder();
      for(JsonElement json : array.getAsJsonArray()) {
        builder.add((BlockPart)context.deserialize(json, BlockPart.class));
      }
      return builder.build();
    }

    throw new JsonSyntaxException("Missing " + name + ", expected to find a JsonArray or JsonObject");
  }

  /** Logic to implement a vanilla block model */
  private static class Loader implements IModelLoader<SimpleBlockModel> {
    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {}

    @Override
    public SimpleBlockModel read(JsonDeserializationContext context, JsonObject json) {
      return deserialize(context, json);
    }
  }
}
