package slimeknights.mantle.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.fluid.transfer.FluidContainerTransferPacket;
import slimeknights.mantle.network.packet.DropLecternBookPacket;
import slimeknights.mantle.network.packet.OpenLecternBookPacket;
import slimeknights.mantle.network.packet.SwingArmPacket;
import slimeknights.mantle.network.packet.UpdateHeldPagePacket;
import slimeknights.mantle.network.packet.UpdateInventoryPagePacket;
import slimeknights.mantle.network.packet.UpdateLecternPagePacket;

/** Handles registering all packets used by Mantle */
public class MantleNetwork {
  /**
   * Network instance
   * 21.0: Initial 1.21.1 release
   */
  private static final String VERSION = "21.0";

  /**
   * Registers packets into this network
   */
  @Internal
  public static void registerPackets(RegisterPayloadHandlersEvent event) {
    PayloadRegistrar registrar = event.registrar(VERSION);

    // to server
    registrar.playToServer(UpdateHeldPagePacket.TYPE, UpdateHeldPagePacket.CODEC, ISimplePacket.handle());
    registrar.playToServer(UpdateInventoryPagePacket.TYPE, UpdateInventoryPagePacket.CODEC, ISimplePacket.handle());
    registrar.playToServer(UpdateLecternPagePacket.TYPE, UpdateLecternPagePacket.CODEC, ISimplePacket.handle());
    registrar.playToServer(DropLecternBookPacket.TYPE, DropLecternBookPacket.CODEC, ISimplePacket.handle());

    // to client
    registrar.playToClient(OpenLecternBookPacket.TYPE, OpenLecternBookPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(SwingArmPacket.TYPE, SwingArmPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(FluidContainerTransferPacket.TYPE, FluidContainerTransferPacket.CODEC, ISimplePacket.handle());
  }
}
