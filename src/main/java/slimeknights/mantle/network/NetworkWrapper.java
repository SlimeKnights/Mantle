package slimeknights.mantle.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.network.packet.ISimplePacket;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A small network implementation/wrapper using Mantle packets.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class NetworkWrapper {
  /** Direction for payload registration. */
  public enum PacketDirection {
    PLAY_TO_CLIENT,
    PLAY_TO_SERVER
  }

  private final ResourceLocation channelName;
  private final String version;
  private final Map<Class<?>,Registration<?>> registrations = new HashMap<>();
  private int id = 0;

  /**
   * Creates a new network wrapper
   * @param channelName  Unique packet channel name
   * @deprecated Give your channel a version number.
   */
  @Deprecated
  public NetworkWrapper(ResourceLocation channelName) {
    this(channelName, "1");
  }

  public NetworkWrapper(ResourceLocation channelName, String version) {
    this.channelName = channelName;
    this.version = version;
  }

  /** Registers stored packets with NeoForge. */
  public void registerPayloads(RegisterPayloadHandlersEvent event) {
    PayloadRegistrar registrar = event.registrar(version).optional();
    registrations.values().forEach(registration -> registration.register(registrar));
  }

  /**
   * Registers a new {@link ISimplePacket}
   * @param clazz    Packet class
   * @param decoder  Packet decoder, typically the constructor
   * @param <MSG>  Packet class type
   */
  public <MSG extends ISimplePacket> void registerPacket(Class<MSG> clazz, Function<FriendlyByteBuf, MSG> decoder, @Nullable PacketDirection direction) {
    registerPacket(clazz, ISimplePacket::encode, decoder, ISimplePacket::handle, direction);
  }

  /**
   * Registers a new generic packet
   * @param clazz      Packet class
   * @param encoder    Encodes a packet to the buffer
   * @param decoder    Packet decoder, typically the constructor
   * @param consumer   Logic to handle a packet
   * @param direction  Network direction for validation. Pass null for bidirectional.
   * @param <MSG>  Packet class type
   */
  public <MSG> void registerPacket(Class<MSG> clazz, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG,IPayloadContext> consumer, @Nullable PacketDirection direction) {
    registerPacketNoLogger(clazz, encoder, wrapLogger(clazz, decoder), consumer, direction);
  }

  /**
   * Registers a new packet without the automatic logging if the decoder fails
   */
  public <MSG> void registerPacketNoLogger(Class<MSG> clazz, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG,IPayloadContext> consumer, @Nullable PacketDirection direction) {
    ResourceLocation typeName = ResourceLocation.fromNamespaceAndPath(channelName.getNamespace(), channelName.getPath() + "/" + id++);
    CustomPacketPayload.Type<Payload<MSG>> type = new CustomPacketPayload.Type<>(typeName);
    registrations.put(clazz, new Registration<>(type, encoder, decoder, consumer, direction));
  }

  /** Wraps the given decoder function */
  private static <MSG> Function<FriendlyByteBuf,MSG> wrapLogger(Class<MSG> clazz, Function<FriendlyByteBuf,MSG> decoder) {
    return buffer -> {
      try {
        return decoder.apply(buffer);
      } catch (Exception e) {
        Mantle.logger.error("Exception while decoding packet of class {}", clazz.getName(), e);
        throw e;
      }
    };
  }


  /* Sending packets */

  /**
   * Sends a packet to the server
   * @param msg  Packet to send
   */
  public void sendToServer(Object msg) {
    PacketDistributor.sendToServer(payload(msg));
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
  public void sendTo(Object msg, Player player) {
    if (player instanceof ServerPlayer serverPlayer) {
      sendTo(msg, serverPlayer);
    }
  }

  /**
   * Sends a packet to a player
   * @param msg     Packet
   * @param player  Player to send
   */
  public void sendTo(Object msg, ServerPlayer player) {
    if (!(player instanceof FakePlayer)) {
      PacketDistributor.sendToPlayer(player, payload(msg));
    }
  }

  /**
   * Sends a packet to players near a location
   * @param msg          Packet to send
   * @param serverWorld  World instance
   * @param position     Position within range
   */
  public void sendToClientsAround(Object msg, ServerLevel serverWorld, BlockPos position) {
    LevelChunk chunk = serverWorld.getChunkAt(position);
    PacketDistributor.sendToPlayersTrackingChunk(serverWorld, chunk.getPos(), payload(msg));
  }

  /**
   * Sends a packet to all entities tracking the given entity
   * @param msg     Packet
   * @param entity  Entity to check
   */
  public void sendToTrackingAndSelf(Object msg, Entity entity) {
    PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload(msg));
  }

  /**
   * Sends a packet to all entities tracking the given entity
   * @param msg     Packet
   * @param entity  Entity to check
   */
  public void sendToTracking(Object msg, Entity entity) {
    PacketDistributor.sendToPlayersTrackingEntity(entity, payload(msg));
  }

  private CustomPacketPayload payload(Object message) {
    Registration<?> registration = registrations.get(message.getClass());
    if (registration == null) {
      throw new IllegalArgumentException("Unregistered packet " + message.getClass().getName());
    }
    return registration.payload(message);
  }

  private static class Payload<MSG> implements CustomPacketPayload {
    private final CustomPacketPayload.Type<Payload<MSG>> type;
    private final MSG message;

    private Payload(CustomPacketPayload.Type<Payload<MSG>> type, MSG message) {
      this.type = type;
      this.message = message;
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
      return type;
    }
  }

  private record Registration<MSG>(
    CustomPacketPayload.Type<Payload<MSG>> type,
    BiConsumer<MSG,FriendlyByteBuf> encoder,
    Function<FriendlyByteBuf,MSG> decoder,
    BiConsumer<MSG,IPayloadContext> consumer,
    @Nullable PacketDirection direction
  ) {
    private StreamCodec<RegistryFriendlyByteBuf,Payload<MSG>> codec() {
      return new StreamCodec<>() {
        @Override
        public Payload<MSG> decode(RegistryFriendlyByteBuf buffer) {
          return new Payload<>(type, decoder.apply(buffer));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, Payload<MSG> payload) {
          encoder.accept(payload.message, buffer);
        }
      };
    }

    private void register(PayloadRegistrar registrar) {
      if (direction == PacketDirection.PLAY_TO_CLIENT) {
        registrar.playToClient(type, codec(), this::handle);
      } else if (direction == PacketDirection.PLAY_TO_SERVER) {
        registrar.playToServer(type, codec(), this::handle);
      } else {
        registrar.playBidirectional(type, codec(), this::handle);
      }
    }

    private void handle(Payload<MSG> payload, IPayloadContext context) {
      consumer.accept(payload.message, context);
    }

    @SuppressWarnings("unchecked")
    private CustomPacketPayload payload(Object message) {
      return new Payload<>(type, (MSG)message);
    }
  }
}
