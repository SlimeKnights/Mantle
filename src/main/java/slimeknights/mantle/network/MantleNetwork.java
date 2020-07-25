package slimeknights.mantle.network;

import net.minecraftforge.fml.network.NetworkDirection;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.network.packet.UpdateSavedPagePacket;

public class MantleNetwork {
  /** Network instance */
  public static final NetworkWrapper INSTANCE = new NetworkWrapper(Mantle.getResource("network"));

  /**
   * Registers packets into this network
   */
  public static void registerPackets() {
    INSTANCE.registerPacket(UpdateSavedPagePacket.class, UpdateSavedPagePacket::new, NetworkDirection.PLAY_TO_SERVER);
  }
}
