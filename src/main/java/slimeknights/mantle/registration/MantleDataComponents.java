package slimeknights.mantle.registration;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.network.MantleStreamCodecs;

/** Handles all custom data component types added by Mantle */
public class MantleDataComponents {
  private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Mantle.modId);

  private MantleDataComponents() {}

  /** Registers this to the bus */
  public static void init(IEventBus bus) {
    DATA_COMPONENTS.register(bus);
  }

  /** Component used by {@link slimeknights.mantle.util.RetexturedHelper} to set the block texture. */
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<Block>> BLOCK_TEXTURE = DATA_COMPONENTS.register("block_texture", () -> DataComponentType.<Block>builder()
    .persistent(BuiltInRegistries.BLOCK.byNameCodec())
    .networkSynchronized(MantleStreamCodecs.BLOCK)
    .build());
}
