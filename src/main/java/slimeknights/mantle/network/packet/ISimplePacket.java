package slimeknights.mantle.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;

/**
 * Packet interface to add common methods for registration
 */
public interface ISimplePacket {
  /**
   * Encodes a packet for the buffer
   * @param buf  Buffer instance
   */
  void encode(PacketByteBuf buf);

  /**
   * Handles receiving the packet
   * @param sender the packet sender
   */
  void handle(PacketSender sender);
}
