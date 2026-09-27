package slimeknights.mantle.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

/**
 * Packet interface to add common methods for registration
 */
public interface ISimplePacket extends CustomPacketPayload {
  /**
   * Handles receiving the packet
   * @param context  Packet context
   */
  void handle(IPayloadContext context);


  /* Helpers */

  IPayloadHandler<ISimplePacket> HANDLE = ISimplePacket::handle;

  /** Creates a casted instance of the handle method. Should only be used for single direction packets */
  @SuppressWarnings("unchecked")
  static <T extends ISimplePacket> IPayloadHandler<T> handle() {
    return (IPayloadHandler<T>) HANDLE;
  }
}
