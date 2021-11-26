package slimeknights.mantle.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullFunction;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.network.MantleNetwork;
import slimeknights.mantle.network.packet.SwingArmPacket;

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
  @CapabilityInject(OffhandCooldownTracker.class)
  public static Capability<OffhandCooldownTracker> CAPABILITY = null;

  /** Registers the capability and subscribes to event listeners */
  public static void register() {
    // register a bunch of dumb unused things because I need to register one actually useful thing
    CapabilityManager.INSTANCE.register(OffhandCooldownTracker.class, new IStorage<OffhandCooldownTracker>() {
      @Nullable
      @Override
      public INBT writeNBT(Capability<OffhandCooldownTracker> capability, OffhandCooldownTracker instance, Direction side) {
        return null;
      }

      @Override
      public void readNBT(Capability<OffhandCooldownTracker> capability, OffhandCooldownTracker instance, Direction side, INBT nbt) {}
    }, () -> new OffhandCooldownTracker(null));

    MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, OffhandCooldownTracker::attachCapability);
  }

  /**
   * Called to add the capability handler to all players
   * @param event  Event
   */
  private static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
    Entity entity = event.getObject();
    if (entity instanceof PlayerEntity) {
      event.addCapability(KEY, new OffhandCooldownTracker((PlayerEntity) entity));
    }
  }

  /** Lazy optional of self for capability requirements */
  private final LazyOptional<OffhandCooldownTracker> capabilityInstance = LazyOptional.of(() -> this);
  /** Player receiving cooldowns */
  @Nullable
  private final PlayerEntity player;
  /** Scale of the last cooldown */
  private int lastCooldown = 0;
  /** Time in ticks when the player can next attack for full power */
  private int attackReady = 0;

  /** Forces the cooldown tracker to enable even if the offhand is not in {@link slimeknights.mantle.data.MantleTags.Items#OFFHAND_COOLDOWN}. Intended to be set in equipment change events, not serialized */
  @Getter @Setter
  private boolean forceEnable = false;

  @Override
  public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
    return cap == CAPABILITY ? this.capabilityInstance.cast() : LazyOptional.empty();
  }

  /** Null safe way to get the player's ticks existed */
  private int getTicksExisted() {
    if (player == null) {
      return 0;
    }
    return player.ticksExisted;
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
   * Returns a number from 0 to 1 denoting the current cooldown amount, akin to {@link PlayerEntity#getCooledAttackStrength(float)}
   * @return  number from 0 to 1, with 1 being no cooldown
   */
  public float getCooldown() {
    int ticksExisted = getTicksExisted();
    if (ticksExisted > this.attackReady || this.lastCooldown == 0) {
      return 1.0f;
    }
    return MathHelper.clamp((this.lastCooldown + ticksExisted - this.attackReady) / (float) this.lastCooldown, 0f, 1f);
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
  public static float getCooldown(PlayerEntity player) {
    return player.getCapability(CAPABILITY).map(COOLDOWN_TRACKER).orElse(1.0f);
  }

  /**
   * Applies cooldown to the given player
   * @param player  Player
   * @param cooldown  Cooldown to apply
   */
  public static void applyCooldown(PlayerEntity player, int cooldown) {
    player.getCapability(CAPABILITY).ifPresent(cap -> cap.applyCooldown(cooldown));
  }

  /**
   * Applies cooldown to the given player
   * @param player  Player
   */
  public static boolean isAttackReady(PlayerEntity player) {
    return player.getCapability(CAPABILITY).map(ATTACK_READY).orElse(true);
  }

  /**
   * Applies cooldown using attack speed
   * @param attackSpeed   Attack speed of the held item
   * @param cooldownTime  Relative cooldown time for the given source, 20 is vanilla
   */
  public static void applyCooldown(PlayerEntity player, float attackSpeed, int cooldownTime) {
    applyCooldown(player, Math.round(cooldownTime / attackSpeed));
  }

  /** Swings the entities hand without resetting cooldown */
  public static void swingHand(LivingEntity entity, Hand hand, boolean updateSelf) {
    if (!entity.isSwingInProgress || entity.swingProgressInt >= entity.getArmSwingAnimationEnd() / 2 || entity.swingProgressInt < 0) {
      entity.swingProgressInt = -1;
      entity.isSwingInProgress = true;
      entity.swingingHand = hand;
      if (!entity.world.isRemote) {
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
