package slimeknights.mantle.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import slimeknights.mantle.util.OffhandCooldownTracker;

/** Packet to tell a client to swing an entity arm, as the vanilla one resets cooldown */
public class SwingArmPacket implements IThreadsafePacket {
  private final int entityId;
  private final InteractionHand hand;

  public SwingArmPacket(Entity entity, InteractionHand hand) {
    this.entityId = entity.getId();
    this.hand = hand;
  }

  public SwingArmPacket(FriendlyByteBuf buffer) {
    this.entityId = buffer.readVarInt();
    this.hand = buffer.readEnum(InteractionHand.class);
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(entityId);
    buffer.writeEnum(hand);
  }

  @Override
  public void handleThreadsafe(Context context) {
    HandleClient.handle(this);
  }

  private static class HandleClient {
    private static void handle(SwingArmPacket packet) {
      Level world = Minecraft.getInstance().level;
      if (world != null) {
        Entity entity = world.getEntity(packet.entityId);
        if (entity instanceof LivingEntity) {
          OffhandCooldownTracker.swingHand((LivingEntity) entity, packet.hand, false);
        }
      }
    }
  }
}
