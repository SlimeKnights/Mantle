package slimeknights.mantle.fluid.transfer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.network.packet.IThreadsafePacket;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Packet to sync fluid container transfer */
public record FluidContainerTransferPacket(Set<Item> items) implements IThreadsafePacket {
  public static final CustomPacketPayload.Type<FluidContainerTransferPacket> TYPE = new CustomPacketPayload.Type<>(Mantle.getResource("fluid_container_transfer"));
  public static final StreamCodec<RegistryFriendlyByteBuf, FluidContainerTransferPacket> STREAM_CODEC = StreamCodec.composite(
    ByteBufCodecs.registry(BuiltInRegistries.ITEM.key()).apply(ByteBufCodecs.list()).map(HashSet::new, List::copyOf), FluidContainerTransferPacket::items,
    FluidContainerTransferPacket::new
  );

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    FluidContainerTransferManager.INSTANCE.setContainerItems(items);
  }
}
