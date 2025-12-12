package slimeknights.mantle.network.packet;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet interface to add common methods for registration.
 * In NeoForge 1.21+, packets implement CustomPacketPayload and use StreamCodec for encoding.
 */
public interface ISimplePacket extends CustomPacketPayload {
  /**
   * Handles receiving the packet on the appropriate side
   * @param context  Payload context providing player, level, and work queue
   */
  void handle(IPayloadContext context);
}
