package slimeknights.mantle.fluid.transfer;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.mantle.network.MantleStreamCodecs;

import java.util.Set;

/** Packet to sync fluid container transfer */
public record FluidContainerTransferPacket(Set<Item> items) implements ISimplePacket {
  public static final Type<FluidContainerTransferPacket> TYPE = new Type<>(Mantle.getResource("sync_fluid_transfers"));
  public static final StreamCodec<RegistryFriendlyByteBuf, FluidContainerTransferPacket> CODEC = StreamCodec.composite(
    MantleStreamCodecs.ITEM.apply(MantleStreamCodecs.set()), FluidContainerTransferPacket::items,
    FluidContainerTransferPacket::new);

  @Override
  public Type<FluidContainerTransferPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    FluidContainerTransferManager.INSTANCE.setContainerItems(items);
  }
}
