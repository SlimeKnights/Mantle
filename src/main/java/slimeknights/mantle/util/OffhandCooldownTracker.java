package slimeknights.mantle.util;

import lombok.RequiredArgsConstructor;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.network.packet.SwingArmPacket;
import slimeknights.mantle.registration.MantleData;

/**
 * Logic to handle offhand having its own cooldown.
 * @see slimeknights.mantle.client.ClientEvents#renderOffhandAttackIndicator(net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Post)
 * @see MantleData#OFFHAND_COOLDOWN
 */
@SuppressWarnings("unused")  // API
@RequiredArgsConstructor
public class OffhandCooldownTracker {
  /** Player receiving cooldowns */
  @Nullable
  private final Player player;
  /** Scale of the last cooldown */
  private int lastCooldown = 0;
  /** Time in ticks when the player can next attack for full power */
  private int attackReady = 0;

  public OffhandCooldownTracker(IAttachmentHolder entity) {
    this(entity instanceof Player p ? p : null);
  }

  /** Null safe way to get the player's ticks existed */
  private int getTicksExisted() {
    if (player == null) {
      return 0;
    }
    return player.tickCount;
  }

  /**
   * Applies the given amount of cooldown
   * @param cooldown  Coolddown amount
   */
  public void applyCooldown(int cooldown) {
    this.lastCooldown = cooldown;
    this.attackReady = getTicksExisted() + cooldown;
  }

  /**
   * Returns a number from 0 to 1 denoting the current cooldown amount, akin to {@link Player#getAttackStrengthScale(float)}
   * @return  number from 0 to 1, with 1 being no cooldown
   */
  public float getCooldown() {
    int ticksExisted = getTicksExisted();
    if (ticksExisted > this.attackReady || this.lastCooldown == 0) {
      return 1.0f;
    }
    return Mth.clamp((this.lastCooldown + ticksExisted - this.attackReady) / (float) this.lastCooldown, 0f, 1f);
  }

  /**
   * Checks if we can perform another attack yet.
   * This counteracts rapid attacks via click macros, in a similar way to vanilla by limiting to once every 10 ticks
   */
  public boolean isAttackReady() {
    return getTicksExisted() + this.lastCooldown > this.attackReady;
  }


  /* Helpers */

  /** Gets the tracker instance for the target entity */
  public static OffhandCooldownTracker get(Player player) {
    return player.getData(MantleData.OFFHAND_COOLDOWN);
  }

  /**
   * Gets the offhand cooldown for the given player
   * @param player  Player
   * @return  Offhand cooldown
   */
  public static float getCooldown(Player player) {
    return get(player).getCooldown();
  }

  /**
   * Applies cooldown to the given player
   * @param player  Player
   * @param cooldown  Cooldown to apply
   */
  public static void applyCooldown(Player player, int cooldown) {
    get(player).applyCooldown(cooldown);
  }

  /**
   * Applies cooldown to the given player
   * @param player  Player
   */
  public static boolean isAttackReady(Player player) {
    return get(player).isAttackReady();
  }

  /**
   * Applies cooldown using attack speed
   * @param attackSpeed   Attack speed of the held item
   * @param cooldownTime  Relative cooldown time for the given source, 20 is vanilla
   */
  public static void applyCooldown(Player player, float attackSpeed, int cooldownTime) {
    applyCooldown(player, Math.round(cooldownTime / attackSpeed));
  }

  /** Swings the entities hand without resetting cooldown */
  public static void swingHand(LivingEntity entity, InteractionHand hand, boolean updateSelf) {
    if (!entity.swinging || entity.swingTime >= entity.getCurrentSwingDuration() / 2 || entity.swingTime < 0) {
      entity.swingTime = -1;
      entity.swinging = true;
      entity.swingingArm = hand;
      if (!entity.level().isClientSide) {
        SwingArmPacket packet = new SwingArmPacket(entity, hand);
        if (updateSelf) {
          PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet);
        } else {
          PacketDistributor.sendToPlayersTrackingEntity(entity, packet);
        }
      }
    }
  }
}
