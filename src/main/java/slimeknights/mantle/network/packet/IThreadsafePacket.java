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
   * @param context  Payload context
   */
  void handleThreadsafe(IPayloadContext context);
}
