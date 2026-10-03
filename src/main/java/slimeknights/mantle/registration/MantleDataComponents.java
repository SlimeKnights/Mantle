package slimeknights.mantle.registration;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
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

  /**
   * Component used by {@link slimeknights.mantle.MantleEvents} to temporarily store the soulbound slot on an item. Is not serialized.
   * May be used by dependencies mods in {@link LivingDeathEvent} to make items soulbound for other reasons.
   */
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SOULBOUND_SLOT = DATA_COMPONENTS.register("soulbound_slot", () -> DataComponentType.<Integer>builder().build());

  /** Component used by {@link slimeknights.mantle.client.book.BookHelper} to store the last viewed page. */
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> BOOK_PAGE = DATA_COMPONENTS.register("book_page", () -> DataComponentType.<String>builder()
    .persistent(Codec.STRING)
    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
    .build());
}
