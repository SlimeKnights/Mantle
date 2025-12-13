package slimeknights.mantle.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.fluid.transfer.FluidContainerTransferPacket;
import slimeknights.mantle.network.packet.DropLecternBookPacket;
import slimeknights.mantle.network.packet.OpenLecternBookPacket;
import slimeknights.mantle.network.packet.OpenNamedBookPacket;
import slimeknights.mantle.network.packet.SwingArmPacket;
import slimeknights.mantle.network.packet.UpdateHeldPagePacket;
import slimeknights.mantle.network.packet.UpdateInventoryPagePacket;
import slimeknights.mantle.network.packet.UpdateLecternPagePacket;

@EventBusSubscriber(modid = Mantle.modId, bus = EventBusSubscriber.Bus.MOD)
public class MantleNetwork {
  /** Network instance */
  public static final NetworkWrapper INSTANCE = new NetworkWrapper(Mantle.modId, "1");

  /**
   * Registers packets via the event handler
   */
  @SubscribeEvent
  public static void registerPackets(RegisterPayloadHandlersEvent event) {
    PayloadRegistrar registrar = event.registrar(INSTANCE.version);
    
    // Client-bound packets
    INSTANCE.registerToClient(OpenLecternBookPacket.TYPE, OpenLecternBookPacket.STREAM_CODEC, OpenLecternBookPacket::handle).accept(registrar);
    INSTANCE.registerToClient(SwingArmPacket.TYPE, SwingArmPacket.STREAM_CODEC, SwingArmPacket::handle).accept(registrar);
    INSTANCE.registerToClient(OpenNamedBookPacket.TYPE, OpenNamedBookPacket.STREAM_CODEC, OpenNamedBookPacket::handle).accept(registrar);
    INSTANCE.registerToClient(FluidContainerTransferPacket.TYPE, FluidContainerTransferPacket.STREAM_CODEC, FluidContainerTransferPacket::handle).accept(registrar);
    
    // Server-bound packets
    INSTANCE.registerToServer(UpdateHeldPagePacket.TYPE, UpdateHeldPagePacket.STREAM_CODEC, UpdateHeldPagePacket::handle).accept(registrar);
    INSTANCE.registerToServer(UpdateInventoryPagePacket.TYPE, UpdateInventoryPagePacket.STREAM_CODEC, UpdateInventoryPagePacket::handle).accept(registrar);
    INSTANCE.registerToServer(UpdateLecternPagePacket.TYPE, UpdateLecternPagePacket.STREAM_CODEC, UpdateLecternPagePacket::handle).accept(registrar);
    INSTANCE.registerToServer(DropLecternBookPacket.TYPE, DropLecternBookPacket.STREAM_CODEC, DropLecternBookPacket::handle).accept(registrar);
  }
}
