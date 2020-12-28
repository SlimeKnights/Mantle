package slimeknights.mantle.client.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IWorld;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.model.fluid.FluidCuboid;
import slimeknights.mantle.client.render.FluidRenderer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Shared logic for fluids rendering in blocks below between Ceramics and Tinkers Construct
 */
public class FaucetFluidLoader extends JsonReloadListener {
  /** GSON instance for this */
  private static final Gson GSON = new GsonBuilder()
    .setPrettyPrinting()
    .disableHtmlEscaping()
    .create();

  /** Singleton instance */
  private static final FaucetFluidLoader INSTANCE = new FaucetFluidLoader();

  /** Name of the default fluid model, shared between Ceramics and Tinkers Construct */
  private static final ResourceLocation DEFAULT_NAME = Mantle.getResource("_default");

  /** Map of fluids */
  private final Map<BlockState,FaucetFluid> fluidMap = new HashMap<>();

  /**
   * Call during mod constructor to enable the faucet fluid loader system
   */
  public static void initialize() {
    Minecraft mc = Minecraft.getInstance();
    //noinspection ConstantConditions
    if (mc != null) {
      IResourceManager manager = mc.getResourceManager();
      if (manager instanceof IReloadableResourceManager) {
        ((IReloadableResourceManager) manager).addReloadListener(FaucetFluidLoader.INSTANCE);
      }
    }
  }

  /** Default fluid model for blocks with no model */
  private FaucetFluid defaultFluid;
  private FaucetFluidLoader() {
    super(GSON, "models/faucet_fluid");
    defaultFluid = FaucetFluid.EMPTY;
  }

  @Override
  protected void apply(Map<ResourceLocation,JsonElement> map, IResourceManager resourceManager, IProfiler profiler) {
    // parse default first
    JsonElement def = map.get(DEFAULT_NAME);
    if (def == null || !def.isJsonObject()) {
      Mantle.logger.warn("Found no default fluid model, this is likely a problem with the resource pack");
      defaultFluid = FaucetFluid.EMPTY;
    } else {
      defaultFluid = FaucetFluid.parseDefault(def.getAsJsonObject());
    }

    // parse remaining models
    for (Entry<ResourceLocation,JsonElement> entry : map.entrySet()) {
      // skip default, already parsed
      ResourceLocation location = entry.getKey();
      if(location.equals(DEFAULT_NAME)) {
        continue;
      }

      // skip if missing required data
      if (!entry.getValue().isJsonObject()) {
        continue;
      }

      JsonObject json = JSONUtils.getJsonObject(entry.getValue(), "");
      JsonObject variants = JSONUtils.getJsonObject(json, "variants");
      try {
        // all others are block
        Block block = ForgeRegistries.BLOCKS.getValue(location);
        if(block != null && block != Blocks.AIR) {
          StateContainer<Block,BlockState> container = block.getStateContainer();
          List<BlockState> validStates = container.getValidStates();
          for (Entry<String, JsonElement> variant : variants.entrySet()) {
            // parse fluid
            FaucetFluid fluid = FaucetFluid.fromJson(JSONUtils.getJsonObject(variant.getValue(), variant.getKey()), defaultFluid);
            validStates.stream().filter(ModelBakery.parseVariantKey(container, variant.getKey())).forEach(state -> fluidMap.put(state, fluid));
          }
        } else {
          Mantle.logger.debug("Skipping loading faucet fluid model '{}' as no coorsponding block exists", location);
        }
      } catch (Exception e) {
        Mantle.logger.warn("Exception loading faucet fluid model '{}': {}", location, e.getMessage());
      }
    }
  }

  /**
   * Gets faucet fluid data for the given block
   * @param state  Block state
   * @return  Faucet fluid data
   */
  public static FaucetFluid get(BlockState state) {
    return INSTANCE.fluidMap.getOrDefault(state, INSTANCE.defaultFluid);
  }

  /**
   * Renders faucet fluids at the relevant location
   * @param world       World instance
   * @param pos         Base position
   * @param direction   Direction to render
   * @param matrices    Matrix instance
   * @param buffer      Builder instance
   * @param still       Still fluid texture
   * @param flowing     Flowing fluid texture
   * @param color       Color to tint fluid
   * @param light       Fluid light value
   */
  public static void renderFaucetFluids(IWorld world, BlockPos pos, Direction direction, MatrixStack matrices, IVertexBuilder buffer, TextureAtlasSprite still, TextureAtlasSprite flowing, int color, int light) {
    int i = 0;
    FaucetFluid faucetFluid;
    do {
      // get the faucet data for the block
      i++;
      faucetFluid = FaucetFluidLoader.get(world.getBlockState(pos.down(i)));
      // render all down cubes with the given offset
      matrices.push();
      matrices.translate(0, -i, 0);
      for (FluidCuboid cube : faucetFluid.getFluids(direction)) {
        FluidRenderer.renderCuboid(matrices, buffer, cube, still, flowing, cube.getFromScaled(), cube.getToScaled(), color, light, false);
      }
      matrices.pop();
    } while (faucetFluid.isContinued());
  }

  /**
   * Data class for faucet data for each region
   */
  public static class FaucetFluid {
    private static final FaucetFluid EMPTY = new FaucetFluid(Collections.emptyList(), Collections.emptyList(), false);
    /** Fluid region below the faucet */
    private final List<FluidCuboid> side;
    /** Fluid region below a centered faucet */
    private final List<FluidCuboid> center;
    /** If true, continues into the block below */
    private final boolean cont;

    public FaucetFluid(List<FluidCuboid> side, List<FluidCuboid> center, boolean cont) {
      this.side = side;
      this.center = center;
      this.cont = cont;
    }

    /**
     * Gets the list of fluids for the given direction
     * @param dir  Faucet direction
     * @return  List of fluids to render below
     */
    public List<FluidCuboid> getFluids(Direction dir) {
      if (dir.getAxis() == Axis.Y) {
        return center;
      }
      return side;
    }

    /**
     * Returns true if the fluid continues into the block below
     * @return  true if continues
     */
    public boolean isContinued() {
      return cont;
    }

    /**
     * Creates a new fluid from JSON, without defaulting for sections. Used for parsing the default
     * @param json  Fluid to create
     * @return  New fluid
     */
    protected static FaucetFluid parseDefault(JsonObject json) {
      List<FluidCuboid> side = FluidCuboid.listFromJson(json, "side");
      List<FluidCuboid> center = FluidCuboid.listFromJson(json, "center");
      return new FaucetFluid(side, center, false);
    }

    /**
     * Creates a new fluid from JSON
     * @param json  Fluid to create
     * @param def   Default fluid for extension
     * @return  New fluid
     */
    protected static FaucetFluid fromJson(JsonObject json, FaucetFluid def) {
      List<FluidCuboid> side = parseFluids(json, "side", def.side);
      List<FluidCuboid> center = parseFluids(json, "center", def.center);
      boolean cont = JSONUtils.getBoolean(json, "continue", false);
      return new FaucetFluid(side, center, cont);
    }

    /**
     * Parses the fluids for the given side, defaulting as relevant
     * @param json  Json object to parse
     * @param tag   Tag name to parse
     * @param def   Default list for this region
     * @return  List of fluid cuboids
     */
    private static List<FluidCuboid> parseFluids(JsonObject json, String tag, List<FluidCuboid> def) {
      JsonElement element;
      if (json.has(tag)) {
        element = json.get(tag);
        // bottom is a keyword that can sub for primitive for both types
      } else if (json.has("bottom") && json.get("bottom").isJsonPrimitive()) {
        element = json.get("bottom");
      } else {
        return def;
      }
      // primitives set the default to the given value
      if (element.isJsonPrimitive()) {
        int value = element.getAsInt();
        return def.stream().map(cuboid -> {
          Vector3f from = cuboid.getFrom().copy();
          from.setY(value);
          return new FluidCuboid(from, cuboid.getTo(), cuboid.getFaces());
        }).collect(Collectors.toList());
      } else {
        return FluidCuboid.listFromJson(json, tag);
      }
    }
  }
}
