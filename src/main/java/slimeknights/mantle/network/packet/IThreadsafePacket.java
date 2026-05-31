package slimeknights.mantle.network.packet;

import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet instance that automatically wraps the logic in {@link IPayloadContext#enqueueWork(Runnable)} for thread safety
 */
public interface IThreadsafePacket extends ISimplePacket {
  @Override
  default void handle(IPayloadContext context) {
    context.enqueueWork(() -> handleThreadsafe(context));
  }

  /**
   * Handles receiving the packet on the correct thread
   * Packet is automatically set to handled as well by the base logic
   * @param context  Packet context
   */
  void handleThreadsafe(IPayloadContext context);
}
