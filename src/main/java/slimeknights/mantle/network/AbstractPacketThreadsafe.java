package slimeknights.mantle.network;

import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.play.ServerPlayNetHandler;

/**
 * Threadsafe integration of the abstract packet.
 * Basically if you're doing something that has any influence on the world you should use this.
 * (That's ~everything)
 */
public abstract class AbstractPacketThreadsafe extends AbstractPacket {

  //TODO fix!
  /*
  @Override
  public final IMessage handleClient(final ClientPlayNetHandler netHandler) {
    FMLCommonHandler.instance().getWorldThread(netHandler).addScheduledTask(new Runnable() {
      @Override
      public void run() {
        handleClientSafe(netHandler);
      }
    });
    return null;
  }

  @Override
  public final IMessage handleServer(final ServerPlayNetHandler netHandler) {
    FMLCommonHandler.instance().getWorldThread(netHandler).addScheduledTask(new Runnable() {
      @Override
      public void run() {
        handleServerSafe(netHandler);
      }
    });
    return null;
  }

  public abstract void handleClientSafe(ClientPlayNetHandler netHandler);

  public abstract void handleServerSafe(ServerPlayNetHandler netHandler);*/
}
