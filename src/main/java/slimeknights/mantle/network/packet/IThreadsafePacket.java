package slimeknights.mantle.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Packet instance that automatically runs the logic on the main thread for thread safety
 */
public interface IThreadsafePacket extends ISimplePacket {
  @Override
  default void handle(PacketSender sender) {
    handleThreadsafe(sender);
//    NetworkEvent.Context context = supplier.get();
//    context.enqueueWork(() -> handleThreadsafe(context));
//    context.setPacketHandled(true);
  }

  /**
   * Handles receiving the packet on the correct thread
   * Packet is automatically set to handled as well by the base logic
   * @param sender the packet sender
   */
  void handleThreadsafe(PacketSender sender);
}
