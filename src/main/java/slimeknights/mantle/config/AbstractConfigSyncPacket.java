package slimeknights.mantle.config;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import slimeknights.mantle.network.AbstractPacket;

public abstract class AbstractConfigSyncPacket extends AbstractPacket {

  private List<AbstractConfigFile> config;

  public AbstractConfigSyncPacket() {
  }

  protected abstract AbstractConfig getConfig();

  @Override
  public IMessage handleClient(NetHandlerPlayClient netHandler) {
    AbstractConfig.syncConfig(getConfig(), config);
    return null;
  }

  @Override
  public IMessage handleServer(NetHandlerPlayServer netHandler) {
    // We sync from server to client, not vice versa
    throw new UnsupportedOperationException("Trying to sync client configs to the server. You registered the packet for the wrong side.");
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    config = new ArrayList<>();
    for(AbstractConfigFile configFile : getConfig().configFileList) {
      int length = buf.readInt();
      byte[] data = new byte[length];
      buf.readBytes(data);
      config.add(configFile.loadFromPacket(data));
    }
  }

  @Override
  public void toBytes(ByteBuf buf) {
    for(AbstractConfigFile configFile : getConfig().configFileList) {
      byte[] data = configFile.getPacketData();
      buf.writeInt(data.length);
      buf.writeBytes(data);
    }
  }
}
