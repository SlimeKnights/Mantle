package slimeknights.mantle.client.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.joml.Vector3f;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.datamap.BlockStateDataMapLoader;
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data class for faucet data for each region.
 * @param side        Fluid cubes for a side pouring facuet.
 * @param center      Fluid cubes for a center pouring faucet.
 * @param isContinued If true, the fluid continues into the next block.
 */
public record FaucetFluid(List<FluidCuboid> side, List<FluidCuboid> center, boolean isContinued) {
  /** Instance with no fluids */
  public static final FaucetFluid EMPTY = new FaucetFluid(List.of(), List.of(), false);
  /** Loader instance */
  public static final RecordLoadable<FaucetFluid> LOADABLE = RecordLoadable.create(
    FluidCuboid.LIST_LOADABLE.requiredField("side", FaucetFluid::side),
    FluidCuboid.LIST_LOADABLE.requiredField("center", FaucetFluid::center),
    BooleanLoadable.INSTANCE.defaultField("continue", false, FaucetFluid::isContinued),
    FaucetFluid::new);
  /** Loader registry for faucet fluids */
  public static final Loader REGISTRY = new Loader();

  /** Used to prevent being initialized multiple times */
  private static boolean initialized = false;

  /**
   * Call during the event to register the reload listener
   */
  public static void initialize(RegisterClientReloadListenersEvent event) {
    if (initialized) {
      return;
    }
    initialized = true;
    event.registerReloadListener(REGISTRY);
  }

  /**
   * Gets the list of fluids for the given direction
   * @param dir Faucet direction
   * @return List of fluids to render below
   */
  public List<FluidCuboid> getFluids(Direction dir) {
    if (dir.getAxis() == Axis.Y) {
      return center;
    }
    return side;
  }

  /**
   * Creates a new fluid from JSON
   * @param json Fluid to create
   * @param def  Default fluid for extension
   * @return New fluid
   */
  public static FaucetFluid fromJson(JsonObject json, FaucetFluid def) {
    List<FluidCuboid> side = parseFluids(json, "side", def.side);
    List<FluidCuboid> center = parseFluids(json, "center", def.center);
    boolean cont = GsonHelper.getAsBoolean(json, "continue", false);
    return new FaucetFluid(side, center, cont);
  }

  /**
   * Parses the fluids for the given side, defaulting as relevant
   * @param json Json object to parse
   * @param tag  Tag name to parse
   * @param def  Default list for this region
   * @return List of fluid cuboids
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
        Vector3f from = new Vector3f(cuboid.getFrom());
        from.y = value;
        return new FluidCuboid(from, cuboid.getTo(), cuboid.getFaces());
      }).collect(Collectors.toList());
    } else {
      return FluidCuboid.LIST_LOADABLE.getIfPresent(json, tag);
    }
  }

  /** Loader implementation */
  public static class Loader extends BlockStateDataMapLoader<FaucetFluid> {
    /** Name of the default fluid model, shared between Ceramics and Tinkers Construct */
    private static final ResourceLocation DEFAULT_NAME = Mantle.getResource("_default");
    @Getter
    private FaucetFluid defaultInstance = FaucetFluid.EMPTY;
    private final DefaultingFaucetFluidLoader dataLoader = new DefaultingFaucetFluidLoader();
    public Loader() {
      super("Faucet Fluids", "mantle/model/faucet_fluid", LOADABLE);
    }

    @Override
    protected RecordLoadable<FaucetFluid> prepareLoader(Map<ResourceLocation,JsonElement> jsons) {
      JsonElement json = jsons.get(DEFAULT_NAME);
      defaultInstance = EMPTY;
      if (json == null) {
        Mantle.logger.warn("Found no default fluid model, this is likely a problem with the resource pack");
      } else {
        try {
          defaultInstance = LOADABLE.convert(json, DEFAULT_NAME.toString());
        } catch (Exception exception) {
          Mantle.logger.error("Failed to load default faucet fluid model {}", DEFAULT_NAME, exception);
        }
      }
      return dataLoader;
    }

    @Nonnull
    @Override
    public FaucetFluid get(BlockState state) {
      return get(state, defaultInstance);
    }

    /** Nested loadable to allow us to swap out the default instance for the deserializer */
    private class DefaultingFaucetFluidLoader implements RecordLoadable<FaucetFluid> {
      @Override
      public FaucetFluid deserialize(JsonObject json, TypedMap context) {
        return fromJson(json, defaultInstance);
      }

      @Override
      public void serialize(FaucetFluid object, JsonObject json) {
        LOADABLE.serialize(object, json);
      }

      @Override
      public FaucetFluid decode(FriendlyByteBuf buffer, TypedMap context) {
        return LOADABLE.decode(buffer, context);
      }

      @Override
      public void encode(FriendlyByteBuf buffer, FaucetFluid value) {
        LOADABLE.encode(buffer, value);
      }
    }
  }
}
