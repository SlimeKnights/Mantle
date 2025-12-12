package slimeknights.mantle.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.OffhandCooldownTracker;

/** Packet to tell a client to swing an entity arm, as the vanilla one resets cooldown */
public record SwingArmPacket(int entityId, InteractionHand hand) implements IThreadsafePacket {
  public static final CustomPacketPayload.Type<SwingArmPacket> TYPE = new CustomPacketPayload.Type<>(Mantle.getResource("swing_arm"));
  public static final StreamCodec<RegistryFriendlyByteBuf, SwingArmPacket> STREAM_CODEC = StreamCodec.composite(
    ByteBufCodecs.VAR_INT, SwingArmPacket::entityId,
    ByteBufCodecs.fromCodec(InteractionHand.CODEC), SwingArmPacket::hand,
    SwingArmPacket::new
  );

  public SwingArmPacket(Entity entity, InteractionHand hand) {
    this(entity.getId(), hand);
  }

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    HandleClient.handle(this);
  }

  private static class HandleClient {
    private static void handle(SwingArmPacket packet) {
      Level world = Minecraft.getInstance().level;
      if (world != null) {
        Entity entity = world.getEntity(packet.entityId);
        if (entity instanceof LivingEntity living) {
          OffhandCooldownTracker.swingHand(living, packet.hand, false);
        }
      }
    }
  }
}
