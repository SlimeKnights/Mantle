package slimeknights.mantle.compat.neoforged.neoforge.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Compatibility context for Mantle's old packet interfaces. */
public final class NetworkEvent {
  private NetworkEvent() {}

  public static class Context {
    private final IPayloadContext context;

    public Context(IPayloadContext context) {
      this.context = context;
    }

    public void enqueueWork(Runnable task) {
      context.enqueueWork(task);
    }

    public void setPacketHandled(boolean handled) {}

    public ServerPlayer getSender() {
      return context.player() instanceof ServerPlayer player ? player : null;
    }
  }
}
