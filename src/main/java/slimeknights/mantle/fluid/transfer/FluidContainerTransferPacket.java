package slimeknights.mantle.fluid.transfer;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.mantle.network.codec.StreamableCodec;
import slimeknights.mantle.recipe.NamedRecipe;

import java.util.List;

/** Packet to sync fluid container transfers */
@Internal
public record FluidContainerTransferPacket(List<NamedRecipe<IFluidContainerTransfer>> transfers) implements ISimplePacket {
  public static final Type<FluidContainerTransferPacket> TYPE = new Type<>(Mantle.getResource("sync_fluid_transfers"));
  public static final StreamCodec<RegistryFriendlyByteBuf, FluidContainerTransferPacket> CODEC = NamedRecipe.streamCodec(new StreamableCodec<>(IFluidContainerTransfer.LOADER))
    .apply(ByteBufCodecs.list()).map(FluidContainerTransferPacket::new, FluidContainerTransferPacket::transfers);

  @Override
  public Type<FluidContainerTransferPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    FluidContainerTransferManager.INSTANCE.setTransfers(transfers);
  }
}
