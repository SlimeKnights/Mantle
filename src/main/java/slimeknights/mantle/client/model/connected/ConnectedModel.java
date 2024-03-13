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
import com.mojang.math.Transformation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import slimeknights.mantle.block.IMultipartConnectedBlock;
import slimeknights.mantle.client.model.util.ColoredBlockModel;
import slimeknights.mantle.client.model.util.DynamicBakedWrapper;
import slimeknights.mantle.client.model.util.ExtraTextureContext;
import slimeknights.mantle.client.model.util.ModelTextureIteratable;
import slimeknights.mantle.client.model.util.SimpleBlockModel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Model that handles generating variants for connected textures
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ConnectedModel implements IUnbakedGeometry<ConnectedModel> {
  /** Loader instance */
  public static IGeometryLoader<ConnectedModel> LOADER = ConnectedModel::deserialize;

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

  /** Map of full texture name to the resulting material, filled during {@link #getMaterials(IGeometryBakingContext, Function, Set)} */
  private Map<String,Material> extraTextures;

  @Override
  public Collection<Material> getMaterials(IGeometryBakingContext owner, Function<ResourceLocation,UnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
    Collection<Material> textures = model.getMaterials(owner, modelGetter, missingTextureErrors);
    // for all connected textures, add suffix textures
    Map<String, Material> extraTextures = new HashMap<>();
    for (Entry<String,String[]> entry : connectedTextures.entrySet()) {
      // fetch data from the base texture
      String name = entry.getKey();
      // skip if missing
      if (!owner.hasMaterial(name)) {
        continue;
      }
      Material base = owner.getMaterial(name);
      ResourceLocation atlas = base.atlasLocation();
      ResourceLocation texture = base.texture();
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
          Material mat;
          // allow overriding a specific texture
          if (owner.hasMaterial(suffixedName)) {
            mat = owner.getMaterial(suffixedName);
          } else {
            mat = new Material(atlas, new ResourceLocation(namespace, path + "/" + suffix));
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
  public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function<Material,TextureAtlasSprite> spriteGetter, ModelState transform, ItemOverrides overrides, ResourceLocation location) {
    BakedModel baked = model.bake(owner, bakery, spriteGetter, transform, overrides, location);
    return new Baked(this, new ExtraTextureContext(owner, extraTextures), transform, baked);
  }

  @SuppressWarnings("WeakerAccess")
  protected static class Baked extends DynamicBakedWrapper<BakedModel> {
    private final ConnectedModel parent;
    private final IGeometryBakingContext owner;
    private final ModelState transforms;
    private final BakedModel[] cache = new BakedModel[64];
    private final Map<String,String> nameMappingCache = new ConcurrentHashMap<>();
    private final ModelTextureIteratable modelTextures;
    public Baked(ConnectedModel parent, IGeometryBakingContext owner, ModelState transforms, BakedModel baked) {
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
        case EAST: return rotation.getCounterClockWise();
        case WEST: return rotation.getClockWise();
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
      return switch (uv.rotation) {
        // 90 degrees
        case 90 -> transform.compose(Direction::getClockWise);
        case 180 -> transform.compose(Direction::getOpposite);
        case 270 -> transform.compose(Direction::getCounterClockWise);
        default -> transform;
      };
    }

    /** Uncached variant of {@link #getConnectedName(String)}, used internally */
    private String getConnectedNameUncached(String key) {
      // otherwise, iterate into the parent models, trying to find a match
      String check = key;
      String found = "";
      for(Map<String, Either<Material, String>> textures : modelTextures) {
        Either<Material, String> either = textures.get(check);
        if (either != null) {
          // if no name, its not connected
          Optional<String> newName = either.right();
          if (newName.isEmpty()) {
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
      return found;
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
      return nameMappingCache.computeIfAbsent(key, this::getConnectedNameUncached);
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
        int flag = 1 << transform.apply(dir).get3DDataValue();
        if ((connections & flag) == flag) {
          key |= 1 << dir.get2DDataValue();
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
    private BakedModel applyConnections(byte connections) {
      // copy each element with updated faces
      List<BlockElement> elements = Lists.newArrayList();
      for (BlockElement part : parent.model.getElements()) {
        Map<Direction,BlockElementFace> partFaces = new EnumMap<>(Direction.class);
        for (Map.Entry<Direction,BlockElementFace> entry : part.faces.entrySet()) {
          // first, determine which texture to use on this side
          Direction dir = entry.getKey();
          BlockElementFace original = entry.getValue();
          BlockElementFace face = original;

          // follow the texture name back to the original name
          // if it never reaches a connected texture, skip
          String connectedTexture = getConnectedName(original.texture);
          if (!connectedTexture.isEmpty()) {
            // if empty string, we can keep the old face
            String suffix = getTextureSuffix(connectedTexture, connections, getTransform(dir, original.uv));
            if (!suffix.isEmpty()) {
              // suffix the texture
              String fullTexture = connectedTexture + suffix;
              face = new BlockElementFace(original.cullForDirection, original.tintIndex, "#" + fullTexture, original.uv);
            }
          }
          // add the updated face
          partFaces.put(dir, face);
        }
        // add the updated parts into a new model part
        elements.add(new BlockElement(part.from, part.to, partFaces, part.rotation, part.shade));
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
          connections |= 1 << dir.get3DDataValue();
        }
      }
      return connections;
    }

    @Nonnull
    @Override
    public ModelData getModelData(BlockAndTintGetter world, BlockPos pos, BlockState state, ModelData tileData) {
      // if the data is already defined, return it, will happen in multipart models
      if (tileData.get(CONNECTIONS) != null) {
        return tileData;
      }

      // gather connections data
      Transformation rotation = transforms.getRotation();
      return tileData.derive()
                     .with(CONNECTIONS, getConnections(dir -> parent.sides.contains(dir) && parent.connectionPredicate.test(state, world.getBlockState(pos.relative(rotation.rotateTransform(dir))))))
                     .build();
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
    protected synchronized List<BakedQuad> getCachedQuads(byte connections, @Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
      // bake a new model if the orientation is not yet baked
      if (cache[connections] == null) {
        cache[connections] = applyConnections(connections);
      }

      // get the model for the given orientation
      return cache[connections].getQuads(state, side, rand, data, renderType);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
      // try model data first
      Byte connections = data.get(CONNECTIONS);
      // if model data failed, try block state
      // temporary fallback until Forge has model data in multipart/weighted random
      if (connections == null) {
        // no state? return original
        if (state == null) {
          return originalModel.getQuads(null, side, rand, data, renderType);
        }
        // this will return original if the state is missing all properties
        Transformation rotation = transforms.getRotation();
        connections = getConnections((dir) -> {
          if (!parent.sides.contains(dir)) {
            return false;
          }
          BooleanProperty prop = IMultipartConnectedBlock.CONNECTED_DIRECTIONS.get(rotation.rotateTransform(dir));
          return state.hasProperty(prop) && state.getValue(prop);
        });
      }
      // get quads using connections
      return getCachedQuads(connections, state, side, rand, data, renderType);
    }
  }

  /** Loader class containing singleton instance */
  public static ConnectedModel deserialize(JsonObject json, JsonDeserializationContext context) {
    ColoredBlockModel model = ColoredBlockModel.deserialize(json, context);

    // root object for all model data
    JsonObject data = GsonHelper.getAsJsonObject(json, "connection");

    // need at least one connected texture
    JsonObject connected = GsonHelper.getAsJsonObject(data, "textures");
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
      JsonArray array = GsonHelper.getAsJsonArray(data, "sides");
      sides = EnumSet.noneOf(Direction.class);
      for (int i = 0; i < array.size(); i++) {
        String side = GsonHelper.convertToString(array.get(i), "sides[" + i + "]");
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
