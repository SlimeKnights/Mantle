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
import com.mojang.math.Transformation;
import lombok.Getter;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel.Builder;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.QuadTransformers;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.geometry.UnbakedGeometryHelper;
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
 * Simpler version of {@link BlockModel} for use in an {@link IUnbakedGeometry}, as the owner handles most block model properties
 */
@SuppressWarnings("WeakerAccess")
public class SimpleBlockModel implements IUnbakedGeometry<SimpleBlockModel> {
  /** Model loader for vanilla block model, mainly intended for use in fallback registration */
  public static final IGeometryLoader<SimpleBlockModel> LOADER = SimpleBlockModel::deserialize;
  /** Location used for baking dynamic models, name does not matter so just using a constant */
  static final ResourceLocation BAKE_LOCATION = Mantle.getResource("dynamic_model_baking");

  /** Parent model location, used to fetch parts and for textures if the owner is not a block model */
  @Getter
  @Nullable
  private ResourceLocation parentLocation;
  /** Model parts for baked model, if empty uses parent parts */
  private final List<BlockElement> parts;
  /** Fallback textures in case the owner does not contain a block model */
  @Getter
  private final Map<String,Either<Material, String>> textures;
  @Getter
  private BlockModel parent;

  /**
   * Creates a new simple block model
   * @param parentLocation  Location of the parent model, if unset has no parent
   * @param textures        List of textures for iteration, in case the owner is not BlockModel
   * @param parts           List of parts in the model
   */
  public SimpleBlockModel(@Nullable ResourceLocation parentLocation, Map<String,Either<Material,String>> textures, List<BlockElement> parts) {
    this.parts = parts;
    this.textures = textures;
    this.parentLocation = parentLocation;
  }

  public SimpleBlockModel(SimpleBlockModel base) {
    this.parts = base.parts;
    this.textures = base.textures;
    this.parentLocation = base.parentLocation;
    this.parent = base.parent;
  }


  /* Properties */

  /**
   * Gets the elements in this simple block model
   * @return  Elements in the model
   */
  @SuppressWarnings("deprecation")
  public List<BlockElement> getElements() {
    return parts.isEmpty() && parent != null ? parent.getElements() : parts;
  }

  /* Textures */

  /**
   * Fetches parent models for this model and its parents
   * @param modelGetter  Model getter function
   */
  public void fetchParent(IGeometryBakingContext owner, Function<ResourceLocation,UnbakedModel> modelGetter) {
    // no work if no parent or the parent is fetched already
    if (parent != null || parentLocation == null) {
      return;
    }

    // iterate through model parents
    Set<UnbakedModel> chain = Sets.newLinkedHashSet();

    // load the first model directly
    parent = getParent(modelGetter, chain, parentLocation, owner.getModelName());
    // null means no model, so set missing
    if (parent == null) {
      parent = getMissing(modelGetter);
      parentLocation = ModelBakery.MISSING_MODEL_LOCATION;
    }

    // loop through each parent, adding in parents
    for (BlockModel link = parent; link.parentLocation != null && link.parent == null; link = link.parent) {
      chain.add(link);

      // fetch model parent
      link.parent = getParent(modelGetter, chain, link.parentLocation, link.name);

      // null means no model, so set missing
      if (link.parent == null) {
        link.parent = getMissing(modelGetter);
        link.parentLocation = ModelBakery.MISSING_MODEL_LOCATION;
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
  private static BlockModel getParent(Function<ResourceLocation,UnbakedModel> modelGetter, Set<UnbakedModel> chain, ResourceLocation location, String name) {
    // model must exist
    UnbakedModel unbaked = modelGetter.apply(location);
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
  private static BlockModel getMissing(Function<ResourceLocation,UnbakedModel> modelGetter) {
    UnbakedModel model = modelGetter.apply(ModelBakery.MISSING_MODEL_LOCATION);
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
  public static Collection<Material> getTextures(IGeometryBakingContext owner, List<BlockElement> elements, Set<Pair<String,String>> missingTextureErrors) {
    // always need a particle texture
    Set<Material> textures = Sets.newHashSet(owner.getMaterial("particle"));
    // iterate all elements, fetching needed textures from the material
    for(BlockElement part : elements) {
      for(BlockElementFace face : part.faces.values()) {
        Material material = owner.getMaterial(face.texture);
        if (Objects.equals(material.texture(), MissingTextureAtlasSprite.getLocation())) {
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
  public Collection<Material> getMaterials(IGeometryBakingContext owner, Function<ResourceLocation,UnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
    this.fetchParent(owner, modelGetter);
    return getTextures(owner, getElements(), missingTextureErrors);
  }


  /* Baking */

  /** Creates a new builder instance from the given context */
  public static SimpleBakedModel.Builder bakedBuilder(IGeometryBakingContext owner, ItemOverrides overrides) {
    return new SimpleBakedModel.Builder(owner.useAmbientOcclusion(), owner.useBlockLight(), owner.isGui3d(), owner.getTransforms(), overrides);
  }

  /**
   * Bakes a single part of the model into the builder
   * @param builder          Baked model builder
   * @param owner            Model owner
   * @param part             Part to bake
   * @param spriteGetter     Sprite getter
   * @param transform        Model transforms
   * @param quadTransformer  Additional forge transforms
   * @param location         Model location
   */
  public static void bakePart(Builder builder, IGeometryBakingContext owner, BlockElement part, Function<Material,TextureAtlasSprite> spriteGetter, ModelState transform, IQuadTransformer quadTransformer, ResourceLocation location) {
    for(Direction direction : part.faces.keySet()) {
      BlockElementFace face = part.faces.get(direction);
      // ensure the name is not prefixed (it always is)
      String texture = face.texture;
      if (texture.charAt(0) == '#') {
        texture = texture.substring(1);
      }
      // bake the face
      TextureAtlasSprite sprite = spriteGetter.apply(owner.getMaterial(texture));
      BakedQuad bakedQuad = BlockModel.bakeFace(part, face, sprite, direction, transform, location);
      quadTransformer.processInPlace(bakedQuad);
      // apply cull face
      //noinspection ConstantConditions  Its nullable, just annotated wrongly
      if (face.cullForDirection == null) {
        builder.addUnculledFace(bakedQuad);
      } else {
        builder.addCulledFace(Direction.rotate(transform.getRotation().getMatrix(), face.cullForDirection), bakedQuad);
      }
    }
  }

  /** Gets the render type group from the given model context */
  public static RenderTypeGroup getRenderTypeGroup(IGeometryBakingContext owner) {
    ResourceLocation renderTypeHint = owner.getRenderTypeHint();
    return renderTypeHint != null ? owner.getRenderType(renderTypeHint) : RenderTypeGroup.EMPTY;
  }

  /**
   * Applies the transformation to the model state for an item layer model.
   */
  public static IQuadTransformer applyTransform(ModelState modelState, Transformation transformation) {
    if (transformation.isIdentity()) {
      return QuadTransformers.empty();
    } else {
      return UnbakedGeometryHelper.applyRootTransform(modelState, transformation);
    }
  }

  /**
   * Bakes a list of block part elements into a model
   * @param owner         Model configuration
   * @param elements      Model elements
   * @param spriteGetter  Sprite getter instance
   * @param transform     Model transform
   * @param overrides     Model overrides
   * @param location      Model bake location
   * @return  Baked model
   */
  public static BakedModel bakeModel(IGeometryBakingContext owner, List<BlockElement> elements, Function<Material,TextureAtlasSprite> spriteGetter, ModelState transform, ItemOverrides overrides, ResourceLocation location) {
    // iterate parts, adding to the builder
    TextureAtlasSprite particle = spriteGetter.apply(owner.getMaterial("particle"));
    SimpleBakedModel.Builder builder = bakedBuilder(owner, overrides).particle(particle);
    IQuadTransformer quadTransformer = applyTransform(transform, owner.getRootTransform());
    for(BlockElement part : elements) {
      bakePart(builder, owner, part, spriteGetter, transform, quadTransformer, location);
    }
    return builder.build(getRenderTypeGroup(owner));
  }

  /**
   * Same as {@link #bakeModel(IGeometryBakingContext, List, Function, ModelState, ItemOverrides, ResourceLocation)}, but passes in sensible defaults for values unneeded in dynamic models
   * @param owner      Model configuration
   * @param elements   Elements to bake
   * @param transform  Model transform
   * @return Baked model
   */
  public static BakedModel bakeDynamic(IGeometryBakingContext owner, List<BlockElement> elements, ModelState transform) {
    return bakeModel(owner, elements, Material::sprite, transform, ItemOverrides.EMPTY, BAKE_LOCATION);
  }

  @Override
  public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function<Material,TextureAtlasSprite> spriteGetter, ModelState transform, ItemOverrides overrides, ResourceLocation location) {
    return bakeModel(owner, this.getElements(), spriteGetter, transform, overrides, location);
  }

  /**
   * Same as {@link #bake(IGeometryBakingContext, ModelBakery, Function, ModelState, ItemOverrides, ResourceLocation)}, but passes in sensible defaults for values unneeded in dynamic models
   * @param owner         Model configuration
   * @param transform     Transform to apply
   * @return  Baked model
   */
  public BakedModel bakeDynamic(IGeometryBakingContext owner, ModelState transform) {
    return bakeDynamic(owner, this.getElements(), transform);
  }


  /* Deserializing */

  /**
   * Deserializes a SimpleBlockModel from JSON
   * @param json     Json element containing the model
   * @param context  Json Context
   * @return  Serialized JSON
   */
  public static SimpleBlockModel deserialize(JsonObject json, JsonDeserializationContext context) {
    // parent, null if missing
    String parentName = GsonHelper.getAsString(json, "parent", "");
    ResourceLocation parent = parentName.isEmpty() ? null : new ResourceLocation(parentName);

    // textures, empty map if missing
    Map<String, Either<Material, String>> textureMap;
    if (json.has("textures")) {
      ImmutableMap.Builder<String, Either<Material, String>> builder = new ImmutableMap.Builder<>();
      ResourceLocation atlas = InventoryMenu.BLOCK_ATLAS;
      JsonObject textures = GsonHelper.getAsJsonObject(json, "textures");
      for(Entry<String, JsonElement> entry : textures.entrySet()) {
        builder.put(entry.getKey(), BlockModel.Deserializer.parseTextureLocationOrReference(atlas, entry.getValue().getAsString()));
      }
      textureMap = builder.build();
    } else {
      textureMap = Collections.emptyMap();
    }

    // elements, empty list if missing
    List<BlockElement> parts;
    if (json.has("elements")) {
      parts = getModelElements(context, GsonHelper.getAsJsonArray(json, "elements"), "elements");
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
  public static List<BlockElement> getModelElements(JsonDeserializationContext context, JsonElement array, String name) {
    // if just one element, array is optional
    if (array.isJsonObject()) {
      return ImmutableList.of(context.deserialize(array.getAsJsonObject(), BlockElement.class));
    }
    // if an array, get array of elements
    if (array.isJsonArray()) {
      ImmutableList.Builder<BlockElement> builder = ImmutableList.builder();
      for(JsonElement json : array.getAsJsonArray()) {
        builder.add((BlockElement)context.deserialize(json, BlockElement.class));
      }
      return builder.build();
    }

    throw new JsonSyntaxException("Missing " + name + ", expected to find a JsonArray or JsonObject");
  }
}
