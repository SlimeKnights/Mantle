package slimeknights.mantle.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.fluid.transfer.FluidContainerTransferPacket;
import slimeknights.mantle.network.packet.DropLecternBookPacket;
import slimeknights.mantle.network.packet.OpenLecternBookPacket;
import slimeknights.mantle.network.packet.OpenNamedBookPacket;
import slimeknights.mantle.network.packet.SwingArmPacket;
import slimeknights.mantle.network.packet.UpdateHeldPagePacket;
import slimeknights.mantle.network.packet.UpdateInventoryPagePacket;
import slimeknights.mantle.network.packet.UpdateLecternPagePacket;
import slimeknights.mantle.network.NetworkWrapper.PacketDirection;

public class MantleNetwork {
  /**
   * Network instance
   * 1: 1.11.101 and before
   * 2: 1.11.102 - New predicate types, enum loadable nullable field optimization
   */
  public static final NetworkWrapper INSTANCE = new NetworkWrapper(Mantle.getResource("network"), "2");
  private static boolean initialized = false;

  /**
   * Registers packets into this network
   */
  public static void init() {
    if (initialized) {
      return;
    }
    initialized = true;
    INSTANCE.registerPacket(OpenLecternBookPacket.class, OpenLecternBookPacket::new, PacketDirection.PLAY_TO_CLIENT);
    INSTANCE.registerPacket(UpdateHeldPagePacket.class, UpdateHeldPagePacket::new, PacketDirection.PLAY_TO_SERVER);
    INSTANCE.registerPacket(UpdateInventoryPagePacket.class, UpdateInventoryPagePacket::new, PacketDirection.PLAY_TO_SERVER);
    INSTANCE.registerPacket(UpdateLecternPagePacket.class, UpdateLecternPagePacket::new, PacketDirection.PLAY_TO_SERVER);
    INSTANCE.registerPacket(DropLecternBookPacket.class, DropLecternBookPacket::new, PacketDirection.PLAY_TO_SERVER);
    INSTANCE.registerPacket(SwingArmPacket.class, SwingArmPacket::new, PacketDirection.PLAY_TO_CLIENT);
    INSTANCE.registerPacket(OpenNamedBookPacket.class, OpenNamedBookPacket::new, PacketDirection.PLAY_TO_CLIENT);
    INSTANCE.registerPacket(FluidContainerTransferPacket.class, FluidContainerTransferPacket::new, PacketDirection.PLAY_TO_CLIENT);
  }

  /** Registers packets with NeoForge's payload system. */
  public static void registerPackets(RegisterPayloadHandlersEvent event) {
    init();
    INSTANCE.registerPayloads(event);
  }
}
