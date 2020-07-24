package slimeknights.mantle.config;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import slimeknights.mantle.network.packet.ISimplePacket;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractConfigSyncPacket implements ISimplePacket {

  private List<AbstractConfigFile> config;

  public AbstractConfigSyncPacket(PacketBuffer buffer) {
    this.config = new ArrayList<>();
    for (AbstractConfigFile configFile : this.getConfig().configFileList) {
      int length = buffer.readInt();
      byte[] data = new byte[length];
      buffer.readBytes(data);
      this.config.add(configFile.loadFromPacket(data));
    }
  }

  public AbstractConfigSyncPacket() {}

  protected boolean sync() {
    return AbstractConfig.syncConfig(this.getConfig(), this.config);
  }

  protected abstract AbstractConfig getConfig();

  @Override
  public void encode(PacketBuffer buf) {
    for (AbstractConfigFile configFile : this.getConfig().configFileList) {
      byte[] data = configFile.getPacketData();
      if (data != null) {
        buf.writeInt(data.length);
        buf.writeBytes(data);
      }
    }
  }

  @Override
  public void handle(Supplier<NetworkEvent.Context> context) {
    context.get().enqueueWork(() -> DistExecutor.unsafeRunForDist(() -> this::sync, () -> {
      throw new UnsupportedOperationException("Trying to sync client configs to the server. You registered the packet for the wrong side.");
    }));

    context.get().setPacketHandled(true);
  }
}
