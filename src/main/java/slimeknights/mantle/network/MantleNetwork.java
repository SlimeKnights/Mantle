package slimeknights.mantle.network;

public class MantleNetwork {
  /** Network instance */
  public static final NetworkWrapper INSTANCE = new NetworkWrapper();

  /**
   * Registers packets into this network
   */
  public static void registerPackets() {
//    INSTANCE.registerPacket(UpdateSavedPagePacket.class, UpdateSavedPagePacket::new, NetworkSide.SERVERBOUND);
  }
}
