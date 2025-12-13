package slimeknights.mantle.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.network.MantleNetwork;
import slimeknights.mantle.network.packet.SwingArmPacket;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Logic to handle offhand having its own cooldown
 */
public class OffhandCooldownTracker {
  public static final ResourceLocation KEY = Mantle.getResource("offhand_cooldown");
  /** @deprecated use {@link #get(Player)} */
  @Deprecated(forRemoval = true)
  public static final Function<OffhandCooldownTracker,Float> COOLDOWN_TRACKER = tracker -> 0f;

  // Deferred Register for Attachments
  private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Mantle.modId);

  public static final DeferredHolder<AttachmentType<?>, AttachmentType<OffhandCooldownTracker>> ATTACHMENT = ATTACHMENT_TYPES.register("offhand_cooldown", () ->
    AttachmentType.builder(OffhandCooldownTracker::new)
      .serialize(OffhandCooldownTracker.CODEC)
      .build()
  );

  public static void register(IEventBus bus) {
    ATTACHMENT_TYPES.register(bus);
  }

  // Codec for persistence
  public static final Codec<OffhandCooldownTracker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    Codec.INT.fieldOf("lastCooldown").forGetter(t -> t.lastCooldown),
    Codec.INT.fieldOf("attackReady").forGetter(t -> t.attackReady),
    Codec.INT.fieldOf("enabled").forGetter(t -> t.enabled)
  ).apply(instance, OffhandCooldownTracker::new));

  /** Scale of the last cooldown */
  private int lastCooldown = 0;
  /** Time in ticks when the player can next attack for full power */
  private int attackReady = 0;

  /** Enables the cooldown tracker if above 0. Intended to be set in equipment change events, not serialized */
  private int enabled = 0;

  public OffhandCooldownTracker() {}

  public OffhandCooldownTracker(int lastCooldown, int attackReady, int enabled) {
    this.lastCooldown = lastCooldown;
    this.attackReady = attackReady;
    this.enabled = enabled;
  }

  /** If true, the tracker is enabled despite a cooldown item not being held */
  @Deprecated(forRemoval = true)
  public boolean isEnabled() {
    return enabled > 0;
  }

  /**
   * Call this method when your item causing offhand cooldown to be needed is enabled and disabled. If multiple placces call this, the tracker will automatically keep enabled until all places disable
   * @param enable  If true, enable. If false, disable
   * @deprecated No longer used, so you can just remove calls.
   */
  @Deprecated(forRemoval = true)
  public void setEnabled(boolean enable) {
    if (enable) {
      enabled++;
    } else {
      enabled--;
    }
  }

  /**
   * Applies the given amount of cooldown
   * @param tickCount Current tick count
   * @param cooldown  Coolddown amount
   */
  public void applyCooldown(int tickCount, int cooldown) {
    this.lastCooldown = cooldown;
    this.attackReady = tickCount + cooldown;
  }

  /**
   * Returns a number from 0 to 1 denoting the current cooldown amount, akin to {@link Player#getAttackStrengthScale(float)}
   * @return  number from 0 to 1, with 1 being no cooldown
   */
  public float getCooldown(int tickCount) {
    int ticksExisted = tickCount;
    if (ticksExisted > this.attackReady || this.lastCooldown == 0) {
      return 1.0f;
    }
    return Mth.clamp((this.lastCooldown + ticksExisted - this.attackReady) / (float) this.lastCooldown, 0f, 1f);
  }

  /**
   * Checks if we can perform another attack yet.
   * This counteracts rapid attacks via click macros, in a similar way to vanilla by limiting to once every 10 ticks
   */
  public boolean isAttackReady(int tickCount) {
    return tickCount + this.lastCooldown > this.attackReady;
  }


  /* Helpers */

  /** Gets the tracker instance for the target entity */
  @Nullable
  public static OffhandCooldownTracker get(@Nullable Player player) {
    if (player == null) {
      return null;
    }
    return player.getData(ATTACHMENT);
  }

  /**
   * Gets the offhand cooldown for the given player
   * @param player  Player
   * @return  Offhand cooldown
   */
  public static float getCooldown(Player player) {
    OffhandCooldownTracker tracker = get(player);
    return tracker != null ? tracker.getCooldown(player.tickCount) : 1.0f;
  }

  /**
   * Applies cooldown to the given player
   * @param player  Player
   * @param cooldown  Cooldown to apply
   */
  public static void applyCooldown(Player player, int cooldown) {
    OffhandCooldownTracker tracker = get(player);
    if (tracker != null) {
      tracker.applyCooldown(player.tickCount, cooldown);
    }
  }

  /**
   * Applies cooldown to the given player
   * @param player  Player
   */
  public static boolean isAttackReady(Player player) {
    OffhandCooldownTracker tracker = get(player);
    return tracker == null || tracker.isAttackReady(player.tickCount);
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
          MantleNetwork.INSTANCE.sendToTrackingAndSelf(packet, entity);
        } else {
          MantleNetwork.INSTANCE.sendToTracking(packet, entity);
        }
      }
    }
  }
}
