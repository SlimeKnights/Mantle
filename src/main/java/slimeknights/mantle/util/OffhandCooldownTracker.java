package slimeknights.mantle.util;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullFunction;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.network.MantleNetwork;
import slimeknights.mantle.network.packet.SwingArmPacket;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Logic to handle offhand having its own cooldown
 */
@RequiredArgsConstructor
public class OffhandCooldownTracker implements ICapabilityProvider {
  public static final ResourceLocation KEY = Mantle.getResource("offhand_cooldown");
  public static final NonNullFunction<OffhandCooldownTracker,Float> COOLDOWN_TRACKER = OffhandCooldownTracker::getCooldown;
  private static final NonNullFunction<OffhandCooldownTracker,Boolean> ATTACK_READY = OffhandCooldownTracker::isAttackReady;

  /**
   * Capability instance for offhand cooldown
   */
  public static final Capability<OffhandCooldownTracker> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

  /** Registers the capability and subscribes to event listeners */
  public static void init() {
    MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, OffhandCooldownTracker::attachCapability);
  }

  /** Registers the capability with the event bus */
  public static void register(RegisterCapabilitiesEvent event) {
    event.register(OffhandCooldownTracker.class);
  }

  /**
   * Called to add the capability handler to all players
   * @param event  Event
   */
  private static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
    Entity entity = event.getObject();
    if (entity instanceof Player player) {
      event.addCapability(KEY, new OffhandCooldownTracker(player));
    }
  }

  /** Lazy optional of self for capability requirements */
  private final LazyOptional<OffhandCooldownTracker> capabilityInstance = LazyOptional.of(() -> this);
  /** Player receiving cooldowns */
  @Nullable
  private final Player player;
  /** Scale of the last cooldown */
  private int lastCooldown = 0;
  /** Time in ticks when the player can next attack for full power */
  private int attackReady = 0;

  /** Enables the cooldown tracker if above 0. Intended to be set in equipment change events, not serialized */
  private int enabled = 0;

  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
    return cap == CAPABILITY ? this.capabilityInstance.cast() : LazyOptional.empty();
  }

  /** Null safe way to get the player's ticks existed */
  private int getTicksExisted() {
    if (player == null) {
      return 0;
    }
    return player.tickCount;
  }

  /** If true, the tracker is enabled despite a cooldown item not being held */
  public boolean isEnabled() {
    return enabled > 0;
  }

  /**
   * Call this method when your item causing offhand cooldown to be needed is enabled and disabled. If multiple placces call this, the tracker will automatically keep enabled until all places disable
   * @param enable  If true, enable. If false, disable
   */
  public void setEnabled(boolean enable) {
    if (enable) {
      enabled++;
    } else {
      enabled--;
    }
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

  /**
   * Gets the offhand cooldown for the given player
   * @param player  Player
   * @return  Offhand cooldown
   */
  public static float getCooldown(Player player) {
    return player.getCapability(CAPABILITY).map(COOLDOWN_TRACKER).orElse(1.0f);
  }

  /**
   * Applies cooldown to the given player
   * @param player  Player
   * @param cooldown  Cooldown to apply
   */
  public static void applyCooldown(Player player, int cooldown) {
    player.getCapability(CAPABILITY).ifPresent(cap -> cap.applyCooldown(cooldown));
  }

  /**
   * Applies cooldown to the given player
   * @param player  Player
   */
  public static boolean isAttackReady(Player player) {
    return player.getCapability(CAPABILITY).map(ATTACK_READY).orElse(true);
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
