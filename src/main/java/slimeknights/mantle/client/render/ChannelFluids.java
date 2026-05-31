package slimeknights.mantle.client.render;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import slimeknights.mantle.data.datamap.RegistryDataMapLoader;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

/** Data class for rendering the fluids in a casting channel */
public record ChannelFluids(FluidCuboid down, Center center, Side side) {
  public static final RecordLoadable<ChannelFluids> LOADABLE = RecordLoadable.create(
    FluidCuboid.LOADABLE.requiredField("down", ChannelFluids::down),
    RecordLoadable.create(
      FluidCuboid.LOADABLE.requiredField("still",   Center::still),
      FluidCuboid.LOADABLE.requiredField("flowing", Center::flowing),
      Center::new).requiredField("center", ChannelFluids::center),
    RecordLoadable.create(
      FluidCuboid.LOADABLE.requiredField("still", Side::still),
      FluidCuboid.LOADABLE.requiredField("edge",  Side::edge),
      FluidCuboid.LOADABLE.requiredField("in",    Side::in),
      FluidCuboid.LOADABLE.requiredField("out",   Side::out),
      Side::new).requiredField("side", ChannelFluids::side),
    ChannelFluids::new);
  /** Registry for loading channel fluids */
  @SuppressWarnings("deprecation")
  public static final RegistryDataMapLoader<Block,ChannelFluids> REGISTRY = new RegistryDataMapLoader<>("Channel fluids", "mantle/model/channel_fluids", BuiltInRegistries.BLOCK, LOADABLE);

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

  /** Gets a fluid for the center */
  public FluidCuboid center(boolean flowing) {
    return center().get(flowing);
  }

  /** Holds the fluids in the center */
  public record Center(FluidCuboid still, FluidCuboid flowing) {
    public FluidCuboid get(boolean flowing) {
      return flowing ? this.flowing : this.still;
    }
  }

  /** Holds the fluids in the center */
  public record Side(FluidCuboid still, FluidCuboid edge, FluidCuboid in, FluidCuboid out) {
    public FluidCuboid flow(boolean out) {
      return out ? this.out : this.in;
    }
  }
}
