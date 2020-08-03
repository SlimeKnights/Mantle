package slimeknights.mantle.client.model.connected;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.BlockFaceUV;
import net.minecraft.client.renderer.model.BlockPart;
import net.minecraft.client.renderer.model.BlockPartFace;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.Plane;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import slimeknights.mantle.block.IMultipartConnectedBlock;
import slimeknights.mantle.client.model.util.ExtraTextureConfiguration;
import slimeknights.mantle.client.model.util.ModelTextureIteratable;
import slimeknights.mantle.client.model.util.SimpleBlockModel;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Model that handles generating variants for connected textures
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ConnectedModel implements IModelGeometry<ConnectedModel> {

  /** Property of the connections cache key. Contains a 6 bit number with each bit representing a direction */
  private static final ModelProperty<Byte> CONNECTIONS = new ModelProperty<>();

  /** Parent model */
  private final SimpleBlockModel model;
  /** Map of texture name to index of suffixes (indexed as 0bENWS) */
  private final Map<String,String[]> connectedTextures;
  /** Function to run to check if this block connects to another */
  private final BiPredicate<BlockState,BlockState> connectionPredicate;
  /** List of sides to check when getting block directions */
  private final Set<Direction> sides;

  /** Map of full texture name to the resulting material, filled during {@link #getTextures(IModelConfiguration, Function, Set)} */
  private Map<String,RenderMaterial> extraTextures;

  @Override
  public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation,IUnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
    Collection<RenderMaterial> textures = model.getTextures(owner, modelGetter, missingTextureErrors);
    // for all connected textures, add suffix textures
    Map<String, RenderMaterial> extraTextures = new HashMap<>();
    for (Entry<String,String[]> entry : connectedTextures.entrySet()) {
      // fetch data from the base texture
      String name = entry.getKey();
      // skip if missing
      if (!owner.isTexturePresent(name)) {
        continue;
      }
      RenderMaterial base = owner.resolveTexture(name);
      ResourceLocation atlas = base.getAtlasLocation();
      ResourceLocation texture = base.getTextureLocation();
      String namespace = texture.getNamespace();
      String path = texture.getPath();

      // use base atlas and texture, but suffix the name
      String[] suffixes = entry.getValue();
      for (String suffix : suffixes) {
        if (suffix.isEmpty()) {
          continue;
        }
        // skip running if we have seen it before
        String suffixedName = name + "_" + suffix;
        if (!extraTextures.containsKey(suffixedName)) {
          RenderMaterial mat;
          // allow overriding a specific texture
          if (owner.isTexturePresent(suffixedName)) {
            mat = owner.resolveTexture(suffixedName);
          } else {
            mat = new RenderMaterial(atlas, new ResourceLocation(namespace, path + "/" + suffix));
          }
          textures.add(mat);
          // cache the texture name, we use it a lot in rebaking
          extraTextures.put(suffixedName, mat);
        }
      }
    }
    // copy into immutable for better performance
    this.extraTextures = ImmutableMap.copyOf(extraTextures);

    // return textures list
    return textures;
  }

  @Override
  public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial,TextureAtlasSprite> spriteGetter, IModelTransform transform, ItemOverrideList overrides, ResourceLocation location) {
    IBakedModel baked = model.bakeModel(owner, transform, overrides, spriteGetter, location);
    return new BakedModel(this, new ExtraTextureConfiguration(owner, extraTextures), transform, baked);
  }

  @SuppressWarnings("WeakerAccess")
  protected static class BakedModel extends BakedModelWrapper<IBakedModel> {
    private final ConnectedModel parent;
    private final IModelConfiguration owner;
    private final IModelTransform transforms;
    private final IBakedModel[] cache = new IBakedModel[64];
    private final Map<String,String> nameMappingCache = new HashMap<>();
    private final ModelTextureIteratable modelTextures;
    protected BakedModel(ConnectedModel parent, IModelConfiguration owner, IModelTransform transforms, IBakedModel baked) {
      super(baked);
      this.parent = parent;
      this.owner = owner;
      this.transforms = transforms;
      this.modelTextures = ModelTextureIteratable.of(owner, parent.model);
      // all directions false gives cache key of 0, that is ourself
      this.cache[0] = baked;
    }

    /**
     * Gets the direction rotated
     * @param direction  Original direction to rotate
     * @param rotation   Rotation origin, aka the face of the block we are looking at. As a result, UP is identity
     * @return  Rotated direction
     */
    private static Direction rotateDirection(Direction direction, Direction rotation) {
      if (rotation == Direction.UP) {
        return direction;
      }
      if (rotation == Direction.DOWN) {
        // Z is backwards on the bottom
        if (direction.getAxis() == Axis.Z) {
          return direction.getOpposite();
        }
        // X is normal
        return direction;
      }
      // sides all just have the next side for left and right, and consistent up and down
      switch(direction) {
        case NORTH: return Direction.UP;
        case SOUTH: return Direction.DOWN;
        case EAST: return rotation.rotateYCCW();
        case WEST: return rotation.rotateY();
      }
      throw new IllegalArgumentException("Direction must be horizontal axis");
    }

    /**
     * Gets a transform function based on the block part UV and block face
     * @param face   Block face in question
     * @param uv     Block UV data
     * @return  Direction transform function
     */
    private static Function<Direction,Direction> getTransform(Direction face, BlockFaceUV uv) {
      // TODO: how do I apply UV lock?
      // final transform switches from face (NSWE) to world direction, the rest are composed in to apply first
      Function<Direction,Direction> transform = (d) -> rotateDirection(d, face);

      // flipping
      boolean flipV = uv.uvs[1] > uv.uvs[3];
      if (uv.uvs[0] > uv.uvs[2]) {
        // flip both
        if (flipV) {
          transform = transform.compose(Direction::getOpposite);
        } else {
          // flip U
          transform = transform.compose((d) -> {
            if (d.getAxis() == Axis.X) {
              return d.getOpposite();
            }
            return d;
          });
        }
      } else if (flipV) {
        transform = transform.compose((d) -> {
          if (d.getAxis() == Axis.Z) {
            return d.getOpposite();
          }
          return d;
        });
      }

      // rotation
      switch (uv.rotation) {
        // 90 degrees
        case 90:
          transform = transform.compose(Direction::rotateY);
          break;
        case 180:
          transform = transform.compose(Direction::getOpposite);
          break;
        case 270:
          transform = transform.compose(Direction::rotateYCCW);
          break;
      }

      return transform;
    }

    /**
     * Gets the name of this texture that supports connected textures, or null if never is connected
     * @param key  Name of the part texture
     * @return  Name of the connected texture
     */
    private String getConnectedName(String key) {
      if (key.charAt(0) == '#') {
        key = key.substring(1);
      }
      // if the name is connected, we are done
      if (parent.connectedTextures.containsKey(key)) {
        return key;
      }

      // if we already found it, return what we found before
      if (nameMappingCache.containsKey(key)) {
        return nameMappingCache.get(key);
      }

      // otherwise, iterate into the parent models, trying to find a match
      String check = key;
      String found = "";
      for(Map<String, Either<RenderMaterial, String>> textures : modelTextures) {
        Either<RenderMaterial, String> either = textures.get(check);
        if (either != null) {
          // if no name, its not connected
          Optional<String> newName = either.right();
          if (!newName.isPresent()) {
            break;
          }
          // if the name is connected, we are done
          check = newName.get();
          if (parent.connectedTextures.containsKey(check)) {
            found = check;
            break;
          }
        }
      }

      // cache what we found
      nameMappingCache.put(key, found);
      return found;
    }

    /**
     * Gets the texture suffix
     * @param texture      Texture name, must be a connected texture
     * @param connections  Connections byte
     * @param transform    Rotations to apply to faces
     * @return  Key used to cache it
     */
    private String getTextureSuffix(String texture, byte connections, Function<Direction,Direction> transform) {
      int key = 0;
      for (Direction dir : Plane.HORIZONTAL) {
        int flag = 1 << transform.apply(dir).getIndex();
        if ((connections & flag) == flag) {
          key |= 1 << dir.getHorizontalIndex();
        }
      }
      // if empty, do not prefix
      String[] suffixes = parent.connectedTextures.get(texture);
      assert suffixes != null;
      String suffix = suffixes[key];
      if (suffix.isEmpty()) {
        return suffix;
      }
      return "_" + suffix;
    }

    /**
     * Gets the model based on the connections in the given model data
     * @param connections  Array of face connections, true at indexes of connected sides
     * @return  Model with connections applied
     */
    private IBakedModel applyConnections(byte connections) {
      // copy each element with updated faces
      List<BlockPart> elements = Lists.newArrayList();
      for (BlockPart part : parent.model.getElements()) {
        Map<Direction,BlockPartFace> partFaces = new EnumMap<>(Direction.class);
        for (Map.Entry<Direction,BlockPartFace> entry : part.mapFaces.entrySet()) {
          // first, determine which texture to use on this side
          Direction dir = entry.getKey();
          BlockPartFace original = entry.getValue();
          BlockPartFace face = original;

          // follow the texture name back to the original name
          // if it never reaches a connected texture, skip
          String connectedTexture = getConnectedName(original.texture);
          if (!connectedTexture.isEmpty()) {
            // if empty string, we can keep the old face
            String suffix = getTextureSuffix(connectedTexture, connections, getTransform(dir, original.blockFaceUV));
            if (!suffix.isEmpty()) {
              // suffix the texture
              String fullTexture = connectedTexture + suffix;
              face = new BlockPartFace(original.cullFace, original.tintIndex, "#" + fullTexture, original.blockFaceUV);
            }
          }
          // add the updated face
          partFaces.put(dir, face);
        }
        // add the updated parts into a new model part
        elements.add(new BlockPart(part.positionFrom, part.positionTo, partFaces, part.partRotation, part.shade));
      }

      // bake the model
      return SimpleBlockModel.bakeDynamic(owner, elements, transforms);
    }

    /**
     * Gets an array of directions to whether a block exists on the side, indexed using direction indexes
     * @param predicate  Function that returns true if the block is connected on the given side
     * @return  Boolean array of data
     */
    private static byte getConnections(Predicate<Direction> predicate) {
      byte connections = 0;
      for (Direction dir : Direction.values()) {
        if (predicate.test(dir)) {
          connections |= 1 << dir.getIndex();
        }
      }
      return connections;
    }

    @Override
    public IModelData getModelData(IBlockDisplayReader world, BlockPos pos, BlockState state, IModelData tileData) {
      // build model data
      IModelData data = tileData;
      if (data == EmptyModelData.INSTANCE) {
        // going to simply add properties below
        data = new ModelDataMap.Builder().build();
      }
      TransformationMatrix rotation = transforms.getRotation();
      data.setData(CONNECTIONS, getConnections((dir) -> parent.sides.contains(dir) && parent.connectionPredicate.test(state, world.getBlockState(pos.offset(rotation.rotateTransform(dir))))));

      return data;
    }


    /**
     * Shared logic to get quads from a connections array
     * @param connections  Byte with 6 bits for the 6 different sides
     * @param state        Block state instance
     * @param side         Cullface
     * @param rand         Random instance
     * @param data         Model data instance
     * @return             Model quads for the given side
     */
    protected List<BakedQuad> getCachedQuads(byte connections, @Nullable BlockState state, @Nullable Direction side, Random rand, IModelData data) {
      // bake a new model if the orientation is not yet baked
      if (cache[connections] == null) {
        cache[connections] = applyConnections(connections);
      }

      // get the model for the given orientation
      return cache[connections].getQuads(state, side, rand, data);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData data) {
      // quick exit if no data
      if (data == EmptyModelData.INSTANCE) {
        return super.getQuads(state, side, rand, data);
      }
      // fetch connections byte from the model data, exit if missing
      Byte connections = data.getData(CONNECTIONS);
      if (connections == null) {
        return super.getQuads(state, side, rand, data);
      }
      // get quads
      return getCachedQuads(connections, state, side, rand, data);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
      // quick exit if no state
      if (state == null) {
        return super.getQuads(null, side, rand);
      }
      // get data based on block state
      TransformationMatrix rotation = transforms.getRotation();
      byte connections = getConnections((dir) -> {
        if (!parent.sides.contains(dir)) {
          return false;
        }
        BooleanProperty prop = IMultipartConnectedBlock.CONNECTED_DIRECTIONS.get(rotation.rotateTransform(dir));
        return state.hasProperty(prop) && state.get(prop);
      });
      return getCachedQuads(connections, state, side, rand, EmptyModelData.INSTANCE);
    }
  }

  /** Loader class containing singleton instance */
  public static class Loader implements IModelLoader<ConnectedModel> {
    /** Shared loader instance */
    public static final ConnectedModel.Loader INSTANCE = new ConnectedModel.Loader();

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {}

    @Override
    public ConnectedModel read(JsonDeserializationContext context, JsonObject json) {
      SimpleBlockModel model = SimpleBlockModel.deserialize(context, json);

      // root object for all model data
      JsonObject data = JSONUtils.getJsonObject(json, "connection");

      // need at least one connected texture
      JsonObject connected = JSONUtils.getJsonObject(data, "textures");
      if (connected.size() == 0) {
        throw new JsonSyntaxException("Must have at least one texture in connected");
      }

      // build texture list
      ImmutableMap.Builder<String,String[]> connectedTextures = new ImmutableMap.Builder<>();
      for (Entry<String,JsonElement> entry : connected.entrySet()) {
        // don't validate texture as it may be contained in a child model that is not yet loaded
        // get type, put in map
        String name = entry.getKey();
        connectedTextures.put(name, ConnectedModelRegistry.deserializeType(entry.getValue(), "textures[" + name + "]"));
      }

      // get a list of sides to pay attention to
      Set<Direction> sides;
      if (data.has("sides")) {
        JsonArray array = JSONUtils.getJsonArray(data, "sides");
        sides = EnumSet.noneOf(Direction.class);
        for (int i = 0; i < array.size(); i++) {
          String side = JSONUtils.getString(array.get(i), "sides[" + i + "]");
          Direction dir = Direction.byName(side);
          if (dir == null) {
            throw new JsonParseException("Invalid side " + side);
          }
          sides.add(dir);
        }
      } else {
        sides = EnumSet.allOf(Direction.class);
      }

      // other data
      BiPredicate<BlockState,BlockState> predicate = ConnectedModelRegistry.deserializePredicate(data, "predicate");

      // final model instance
      return new ConnectedModel(model, connectedTextures.build(), predicate, sides);
    }
  }
}
