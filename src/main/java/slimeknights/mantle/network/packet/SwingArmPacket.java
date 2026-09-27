package slimeknights.mantle.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.mantle.network.MantleStreamCodecs;
import slimeknights.mantle.util.OffhandCooldownTracker;

/** Packet to tell a client to swing an entity arm, as the vanilla one resets cooldown */
public record SwingArmPacket(int entityId, InteractionHand hand) implements ISimplePacket {
  public static final Type<SwingArmPacket> TYPE = new Type<>(Mantle.getResource("swing_arm"));
  public static final StreamCodec<ByteBuf, SwingArmPacket> CODEC = StreamCodec.composite(
    ByteBufCodecs.VAR_INT, SwingArmPacket::entityId,
    MantleStreamCodecs.INTERACTION_HAND, SwingArmPacket::hand,
    SwingArmPacket::new);

  public SwingArmPacket(Entity entity, InteractionHand hand) {
    this(entity.getId(), hand);
  }

  @Override
  public Type<SwingArmPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    Level level = context.player().level();
    Entity entity = level.getEntity(entityId);
    if (entity instanceof LivingEntity living) {
      OffhandCooldownTracker.swingHand(living, hand, false);
    }
  }
}
