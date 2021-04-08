package slimeknights.mantle.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.network.packet.ISimplePacket;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A small network implementation/wrapper using AbstractPackets instead of IMessages.
 * Instantiate in your mod class and register your packets accordingly.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class NetworkWrapper {
  private static final String PROTOCOL_VERSION = "1";

  private final List<BiConsumer<Object, PacketByteBuf>> encoders = new ArrayList<>();

  /**
   * Creates a new network wrapper
   */
  public NetworkWrapper() {
  }

  /**
   * Registers a new generic packet
   * @param clazz      Packet class
   * @param encoder    Encodes a packet to the buffer
   * @param decoder    Packet decoder, typically the constructor
   * @param direction  Network direction for validation. Pass null for no direction
   */
  public void registerPacket(Class<Object> clazz, BiConsumer<Object, PacketByteBuf> encoder, Function<PacketByteBuf, Object> decoder, BiConsumer<Object, PacketSender> consumer, @Nullable NetworkSide direction) {
    //Workaround for the current system
    Identifier channelName = Mantle.getResource(clazz.getSimpleName());

    encoders.add(encoder);

    if(direction == NetworkSide.SERVERBOUND) {
      ServerPlayNetworking.registerGlobalReceiver(channelName, (minecraftServer, serverPlayerEntity, serverPlayNetworkHandler, packetByteBuf, packetSender) -> consumer.accept(decoder.apply(packetByteBuf), packetSender));
    }
    if(direction == NetworkSide.CLIENTBOUND) {
      ClientPlayNetworking.registerGlobalReceiver(channelName, (minecraftClient, clientPlayNetworkHandler, packetByteBuf, packetSender) -> consumer.accept(decoder.apply(packetByteBuf), packetSender));
    }
  }


  /* Sending packets */

  /**
   * Sends a packet to the server
   * @param msg  Packet to send
   */
  public void sendToServer(Object msg) {
    PacketByteBuf packetByteBuf = PacketByteBufs.create();
    Identifier channelName = Mantle.getResource(msg.getClass().getSimpleName());
    for (BiConsumer<Object, PacketByteBuf> encoder : encoders) {
      encoder.accept(msg, packetByteBuf);
    }
    ClientPlayNetworking.send(channelName, packetByteBuf);
  }

  /**
   * Sends a packet to the given packet distributor
   * @param target   Packet target
   * @param msg  Packet to send
   */
  public void send(ServerPlayerEntity target, Object msg) {
    PacketByteBuf packetByteBuf = PacketByteBufs.create();
    Identifier channelName = Mantle.getResource(msg.getClass().getSimpleName());
    for (BiConsumer<Object, PacketByteBuf> encoder : encoders) {
      encoder.accept(msg, packetByteBuf);
    }

    ServerPlayNetworking.send(target, channelName, packetByteBuf);
  }

  /**
   * Sends a vanilla packet to the given entity
   * @param player  Player receiving the packet
   * @param packet  Packet
   */
  public void sendVanillaPacket(Packet<?> packet, Entity player) {
    if (player instanceof ServerPlayerEntity && ((ServerPlayerEntity) player).networkHandler != null) {
      ((ServerPlayerEntity) player).networkHandler.sendPacket(packet);
    }
  }

  /**
   * Sends a packet to a player
   * @param msg     Packet
   * @param player  Player to send
   */
  public void sendTo(Object msg, ServerPlayerEntity player) {
    send(player, msg);
  }

  /**
   * Sends a packet to players near a location
   * @param msg          Packet to send
   * @param serverWorld  World instance
   * @param position     Position within range
   */
  public void sendToClientsAround(Object msg, ServerWorld serverWorld, BlockPos position) {
    for (ServerPlayerEntity playerEntity : PlayerLookup.around(serverWorld, position, 16)) {
      send(playerEntity, msg);
    }
  }
}
