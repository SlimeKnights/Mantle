package slimeknights.mantle.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import slimeknights.mantle.network.packet.ISimplePacket;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * A network implementation/wrapper using the NeoForge 1.21+ payload system.
 * Instantiate in your mod class and register your packets via the RegisterPayloadHandlersEvent.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class NetworkWrapper {
  /** Mod ID for this network */
  private final String modId;
  /** Network version */
  public final String version;
  /** Registrar instance, only valid during event handling */
  @Nullable
  private PayloadRegistrar registrar;

  /**
   * Creates a new network wrapper
   * @param modId    Mod ID for packet registration
   * @param version  Network protocol version
   */
  public NetworkWrapper(String modId, String version) {
    this.modId = modId;
    this.version = version;
  }

  /**
   * Creates a new network wrapper
   * @param channelName  Unique packet channel name (namespace will be used as mod ID)
   * @deprecated Use constructor with modId and version instead
   */
  @Deprecated
  public NetworkWrapper(ResourceLocation channelName) {
    this(channelName.getNamespace(), "1");
  }

  /**
   * Creates a new network wrapper
   * @param channelName  Unique packet channel name (namespace will be used as mod ID)
   * @param version      Network protocol version
   * @deprecated Use constructor with modId and version instead
   */
  @Deprecated
  public NetworkWrapper(ResourceLocation channelName, String version) {
    this(channelName.getNamespace(), version);
  }

  /**
   * Call this method from your RegisterPayloadHandlersEvent handler to set up the registrar.
   * After calling this, use registerPacket to register your packets, then call finishRegistration.
   * @param event The RegisterPayloadHandlersEvent
   */
  public void setup(RegisterPayloadHandlersEvent event) {
    this.registrar = event.registrar(this.version);
  }

  /**
   * Registers a new {@link ISimplePacket} that can be sent both directions
   * @param type         Packet type
   * @param streamCodec  Stream codec for encoding/decoding
   * @param <MSG>        Packet class type
   */
  public <MSG extends ISimplePacket> void registerPacket(CustomPacketPayload.Type<MSG> type, StreamCodec<? super RegistryFriendlyByteBuf, MSG> streamCodec) {
    if (registrar == null) {
      throw new IllegalStateException("Must call setup() before registering packets");
    }
    registrar.playBidirectional(type, streamCodec, (packet, context) -> packet.handle(context));
  }

  /**
   * Registers a new {@link ISimplePacket} that is sent to the client
   * @param type         Packet type
   * @param streamCodec  Stream codec for encoding/decoding
   * @param <MSG>        Packet class type
   */
  public <MSG extends ISimplePacket> void registerClientbound(CustomPacketPayload.Type<MSG> type, StreamCodec<? super RegistryFriendlyByteBuf, MSG> streamCodec) {
    if (registrar == null) {
      throw new IllegalStateException("Must call setup() before registering packets");
    }
    registrar.playToClient(type, streamCodec, (packet, context) -> packet.handle(context));
  }

  /**
   * Registers a new {@link ISimplePacket} that is sent to the server
   * @param type         Packet type
   * @param streamCodec  Stream codec for encoding/decoding
   * @param <MSG>        Packet class type
   */
  public <MSG extends ISimplePacket> void registerServerbound(CustomPacketPayload.Type<MSG> type, StreamCodec<? super RegistryFriendlyByteBuf, MSG> streamCodec) {
    if (registrar == null) {
      throw new IllegalStateException("Must call setup() before registering packets");
    }
    registrar.playToServer(type, streamCodec, (packet, context) -> packet.handle(context));
  }

  /**
   * Creates a registration consumer for client-bound packets
   * @param type         Packet type
   * @param streamCodec  Stream codec for encoding/decoding
   * @param handler      Packet handler
   * @param <MSG>        Packet class type
   * @return Consumer that accepts a PayloadRegistrar to complete registration
   */
  public <MSG extends CustomPacketPayload> Consumer<PayloadRegistrar> registerToClient(
      CustomPacketPayload.Type<MSG> type,
      StreamCodec<? super RegistryFriendlyByteBuf, MSG> streamCodec,
      IPayloadHandler<MSG> handler) {
    return reg -> reg.playToClient(type, streamCodec, handler);
  }

  /**
   * Creates a registration consumer for server-bound packets
   * @param type         Packet type
   * @param streamCodec  Stream codec for encoding/decoding
   * @param handler      Packet handler
   * @param <MSG>        Packet class type
   * @return Consumer that accepts a PayloadRegistrar to complete registration
   */
  public <MSG extends CustomPacketPayload> Consumer<PayloadRegistrar> registerToServer(
      CustomPacketPayload.Type<MSG> type,
      StreamCodec<? super RegistryFriendlyByteBuf, MSG> streamCodec,
      IPayloadHandler<MSG> handler) {
    return reg -> reg.playToServer(type, streamCodec, handler);
  }

  /**
   * Creates a registration consumer for bidirectional packets
   * @param type         Packet type
   * @param streamCodec  Stream codec for encoding/decoding
   * @param handler      Packet handler
   * @param <MSG>        Packet class type
   * @return Consumer that accepts a PayloadRegistrar to complete registration
   */
  public <MSG extends CustomPacketPayload> Consumer<PayloadRegistrar> registerBidirectional(
      CustomPacketPayload.Type<MSG> type,
      StreamCodec<? super RegistryFriendlyByteBuf, MSG> streamCodec,
      IPayloadHandler<MSG> handler) {
    return reg -> reg.playBidirectional(type, streamCodec, handler);
  }


  /* Sending packets */

  /**
   * Sends a packet to the server
   * @param msg  Packet to send
   */
  public void sendToServer(CustomPacketPayload msg) {
    PacketDistributor.sendToServer(msg);
  }

  /**
   * Sends a vanilla packet to the given entity
   * @param player  Player receiving the packet
   * @param packet  Packet
   */
  public void sendVanillaPacket(Packet<?> packet, Entity player) {
    if (player instanceof ServerPlayer sPlayer) {
      sPlayer.connection.send(packet);
    }
  }

  /**
   * Sends a packet to a player
   * @param msg     Packet
   * @param player  Player to send
   */
  public void sendTo(CustomPacketPayload msg, Player player) {
    if (player instanceof ServerPlayer serverPlayer) {
      sendTo(msg, serverPlayer);
    }
  }

  /**
   * Sends a packet to a player
   * @param msg     Packet
   * @param player  Player to send
   */
  public void sendTo(CustomPacketPayload msg, ServerPlayer player) {
    if (!(player instanceof FakePlayer)) {
      PacketDistributor.sendToPlayer(player, msg);
    }
  }

  /**
   * Sends a packet to players near a location
   * @param msg          Packet to send
   * @param serverWorld  World instance
   * @param position     Position within range
   */
  public void sendToClientsAround(CustomPacketPayload msg, ServerLevel serverWorld, BlockPos position) {
    PacketDistributor.sendToPlayersTrackingChunk(serverWorld, new ChunkPos(position), msg);
  }

  /**
   * Sends a packet to all entities tracking the given entity
   * @param msg     Packet
   * @param entity  Entity to check
   */
  public void sendToTrackingAndSelf(CustomPacketPayload msg, Entity entity) {
    PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, msg);
  }

  /**
   * Sends a packet to all entities tracking the given entity
   * @param msg     Packet
   * @param entity  Entity to check
   */
  public void sendToTracking(CustomPacketPayload msg, Entity entity) {
    PacketDistributor.sendToPlayersTrackingEntity(entity, msg);
  }
}
