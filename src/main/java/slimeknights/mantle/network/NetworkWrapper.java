package slimeknights.mantle.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A small network implementation/wrapper using AbstractPackets instead of IMessages.
 * Instantiate in your mod class and register your packets accordingly.
 */
public class NetworkWrapper {

  public final SimpleChannel network;
  private int id = 0;
  private static final String PROTOCOL_VERSION = Integer.toString(1);

  public NetworkWrapper(String channelName) {
    network = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(channelName))
        .clientAcceptedVersions(PROTOCOL_VERSION::equals)
        .serverAcceptedVersions(PROTOCOL_VERSION::equals)
        .networkProtocolVersion(() -> PROTOCOL_VERSION)
        .simpleChannel();
  }

  public <MSG> void registerPacket(Class<MSG> packetClazz, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
    network.registerMessage(id++, packetClazz, encoder, decoder, messageConsumer);
  }

  public void sendToServer(Object msg) {
    network.sendToServer(msg);
  }

  public void sendTo(Object msg, EntityPlayerMP player) {
    if(!(player instanceof FakePlayer)) {
      network.sendTo(msg, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }
  }
}
