package slimeknights.mantle.util;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Helpers for attacking with weapons */
public class CombatHelper {
  private static final float TO_RADIAN = (float)Math.PI / 180f;
  /** Attribute modifier to disable knockback on a target */
  private static final AttributeModifier ANTI_KNOCKBACK_MODIFIER = new AttributeModifier(Mantle.getResource("anti_knockback"), 1f, Operation.ADD_VALUE);
  /** Tool action to disable the base knockback of the weapon. Requires replacing left click behavior of your weapon. */
  public static final ItemAbility NO_BASE_KNOCKBACK = ItemAbility.get("no_base_knockback");

  private CombatHelper() {}

  /** Gets the item stack in the main hand that contributes to attributes. Exposed for benefit of Tinkers' Construct which can optimize these methods for its tools. */
  public static ItemStack getMainhandAttributeStack(LivingEntity entity) {
    // clientside does not use last item stack, so our best choice is the mainhand stack
    if (entity.level().isClientSide) {
      return entity.getMainHandItem();
    }
    // serverside, use the last item stack instead of the current. Should be the same, but if they mismatch then last item stack has correct attributes
    return entity.getMainHandItem();
  }

  /**
   * Gets a modifiable map that is a copy of the modifiers from the given attribute instance. All operations are guaranteed to have a valid set.
   * Note we use a map instead of a full attribute instance as we don't need the cache or other data structures.
   */
  public static Map<Operation, Set<AttributeModifier>> copyModifiers(AttributeInstance instance) {
    Map<Operation, Set<AttributeModifier>> modifiers = new EnumMap<>(Operation.class);
    for (Operation operation : Operation.values()) {
      modifiers.put(operation, new HashSet<>());
    }
    for (AttributeModifier modifier : instance.getModifiers()) {
      modifiers.get(modifier.operation()).add(modifier);
    }
    return modifiers;
  }

  /** Gets the attribute for the offhand by subtracting mainhand attributes and adding in offhand stack attributes. */
  public static float getOffhandAttribute(ItemStack stack, LivingEntity entity, Holder<Attribute> attribute) {
    AttributeInstance instance = entity.getAttribute(attribute);
    if (instance == null) {
      return (float) entity.getAttributeBaseValue(attribute);
    }

    // fetch attributes for both relevant stacks
    ItemStack mainStack = getMainhandAttributeStack(entity);
    Collection<AttributeModifier> mainModifiers = List.of();
    if (!mainStack.isEmpty()) {
      mainModifiers = getAttributeModifiers(mainStack, attribute);
    }
    Collection<AttributeModifier> offhandModifiers = getAttributeModifiers(stack, attribute);

    // if no modifier changed, can save some work by just using the cached value
    if (mainModifiers.isEmpty() && offhandModifiers.isEmpty()) {
      return (float) instance.getValue();
    }

    // start by creating a modifiable copy of the per operation attribute map
    Map<Operation, Set<AttributeModifier>> modifiers = copyModifiers(instance);
    // remove all mainhand modifiers
    for (AttributeModifier modifier : mainModifiers) {
      modifiers.get(modifier.operation()).remove(modifier);
    }
    // add in all offhand modifiers
    for (AttributeModifier modifier : offhandModifiers) {
      // while there should be no duplicates due to mainhand modifiers above,
      // this will remove duplicates due to AttributeModifier equals only checking UUID
      modifiers.get(modifier.operation()).add(modifier);
    }
    // compute the value
    return (float) computeAttribute(attribute, instance.getBaseValue(), modifiers);
  }

  /** Gets attribute modifiers on the stack for the main hand slot. */
  private static Collection<AttributeModifier> getAttributeModifiers(ItemStack stack, Holder<Attribute> attribute) {
    List<AttributeModifier> modifiers = new ArrayList<>();
    stack.forEachModifier(EquipmentSlot.MAINHAND, (candidate, modifier) -> {
      if (candidate.equals(attribute)) {
        modifiers.add(modifier);
      }
    });
    return modifiers;
  }

  /** Computes the value for the given attribute. Copied from {@link AttributeInstance#calculateValue} */
  public static double computeAttribute(Holder<Attribute> attribute, double base, Map<Operation,Set<AttributeModifier>> modifiers) {
    // addition modifiers
    for (AttributeModifier modifier : modifiers.get(Operation.ADD_VALUE)) {
      base += modifier.amount();
    }
    // multiply base
    double value = base;
    for (AttributeModifier modifier : modifiers.get(Operation.ADD_MULTIPLIED_BASE)) {
      value += base * modifier.amount();
    }
    // multiply total
    for (AttributeModifier modifier : modifiers.get(Operation.ADD_MULTIPLIED_TOTAL)) {
      value *= 1.0 + modifier.amount();
    }
    return attribute.value().sanitizeValue(value);
  }

  /** Checks if the given entity can be attacked. */
  public static boolean isAttackable(Entity attacker, Entity target) {
    return target.isAttackable() && !target.skipAttackInteraction(attacker);
  }

  /**
   * Performs an attack, mimicking  {@link Player#attack(Entity)}.
   * For use in {@link net.minecraft.world.item.Item#interactLivingEntity(ItemStack, Player, LivingEntity, InteractionHand)} primarily,
   * but can also be used to fake an attack similar to {@link net.neoforged.neoforge.common.extensions.IForgeItem#onLeftClickEntity(ItemStack, Player, Entity)}.
   *
   * @param stack         Stack used for attacking.
   * @param target        Entity target
   * @param targetLiving  Living entity target. May be different in the case of multipart entities.
   * @param hand          Hand used for attacking.
   */
  public static boolean attack(ItemStack stack, Player player, Entity target, @Nullable LivingEntity targetLiving, InteractionHand hand) {
    return attack(stack, player, target, targetLiving, hand, player.damageSources().playerAttack(player));
  }

  /**
   * Performs an attack, mimicking {@link Player#attack(Entity)} but allowing the damage source to be swapped.
   * For use in {@link net.minecraft.world.item.Item#interactLivingEntity(ItemStack, Player, LivingEntity, InteractionHand)} primarily,
   * but can also be used to fake an attack similar to {@link net.neoforged.neoforge.common.extensions.IForgeItem#onLeftClickEntity(ItemStack, Player, Entity)}.
   *
   * @param stack         Stack used for attacking.
   * @param target        Entity target
   * @param targetLiving  Living entity target. May be different in the case of multipart entities.
   * @param hand          Hand used for attacking.
   * @param damageSource  Damage source to apply
   */
  public static boolean attack(ItemStack stack, Player player, Entity target, @Nullable LivingEntity targetLiving, InteractionHand hand, DamageSource damageSource) {
    if (isAttackable(player, target)) {
      // find damage to deal
      float damage;
      if (hand == InteractionHand.OFF_HAND) {
        damage = getOffhandAttribute(stack, player, Attributes.ATTACK_DAMAGE);
      } else {
        damage = (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
      }

      // find enchantment damage
      float enchantmentDamage = 0;
      if (player.level() instanceof ServerLevel serverLevel) {
        enchantmentDamage = EnchantmentHelper.modifyDamage(serverLevel, stack, target, damageSource, damage) - damage;
      }

      // scale damage cooldown
      float cooldown = hand == InteractionHand.OFF_HAND ? OffhandCooldownTracker.getCooldown(player) : player.getAttackStrengthScale(0.5F);
      damage *= 0.2F + cooldown * cooldown * 0.8F;
      enchantmentDamage *= cooldown;
      if (damage > 0.0F || enchantmentDamage > 0.0F) {
        boolean fullyCharged = cooldown > 0.9F;

        // find knockback
        float knockback;
        if (hand == InteractionHand.OFF_HAND) {
          knockback = getOffhandAttribute(stack, player, Attributes.ATTACK_KNOCKBACK);
        } else {
          knockback = (float) player.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        }

        if (player.level() instanceof ServerLevel serverLevel) {
          knockback = EnchantmentHelper.modifyKnockback(serverLevel, stack, target, damageSource, knockback);
        }
        boolean sprinting = false;
        if (player.isSprinting() && fullyCharged) {
          player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, player.getSoundSource(), 1.0F, 1.0F);
          knockback += 1;
          sprinting = true;
        }

        // find critical
        boolean critical = fullyCharged && player.fallDistance > 0.0F && !player.onGround() && !player.onClimbable() && !player.isSprinting() && !player.isInWater() && !player.hasEffect(MobEffects.BLINDNESS) && !player.isPassenger() && targetLiving != null;
        CriticalHitEvent hitResult = CommonHooks.fireCriticalHit(player, target, critical, critical ? 1.5f : 1f);
        critical = hitResult.isCriticalHit();
        if (critical) {
          damage *= hitResult.getDamageMultiplier();
        }

        // finish damage enchantments
        damage += enchantmentDamage;

        // check if we can do a sweep attack
        boolean canSweep = fullyCharged && !(critical && hitResult.disableSweep()) && !sprinting && player.onGround() && (player.walkDist - player.walkDistO) < player.getSpeed() && stack.canPerformAction(ItemAbilities.SWORD_SWEEP);
        canSweep = CommonHooks.fireSweepAttack(player, target, canSweep).isSweeping();

        // apply fire aspect and fetch health
        float health = 0.0F;
        if (targetLiving != null) {
          health = targetLiving.getHealth();
        }

        // hit the target
        Vec3 movement = target.getDeltaMovement();
        boolean hit;

        // cancel knockback if requested
        if (stack.canPerformAction(NO_BASE_KNOCKBACK) && targetLiving != null) {
          AttributeInstance knockbackAttribute = targetLiving.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
          if (knockbackAttribute != null && !knockbackAttribute.hasModifier(ANTI_KNOCKBACK_MODIFIER.id())) {
            knockbackAttribute.addTransientModifier(ANTI_KNOCKBACK_MODIFIER);
            hit = target.hurt(damageSource, damage);
            knockbackAttribute.removeModifier(ANTI_KNOCKBACK_MODIFIER);
          } else {
            hit = target.hurt(damageSource, damage);
          }
        } else {
          hit = target.hurt(damageSource, damage);
        }

        // apply hit effects
        if (hit) {
          // apply knockback
          if (knockback > 0) {
            if (targetLiving != null) {
              targetLiving.knockback(knockback * 0.5f, Mth.sin(player.getYRot() * TO_RADIAN), -Mth.cos(player.getYRot() * TO_RADIAN));
            } else {
              target.push(-Mth.sin(player.getYRot() * TO_RADIAN) * knockback * 0.5F, 0.1D, Mth.cos(player.getYRot() * TO_RADIAN) * knockback * 0.5f);
            }

            player.setDeltaMovement(player.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
            player.setSprinting(false);
          }

          // sweep attack
          if (canSweep) {
            float sweepDamage = 1 + (float)player.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO) * damage;
            for (LivingEntity living : player.level().getEntitiesOfClass(LivingEntity.class, stack.getSweepHitBox(player, target))) {
              double entityReachSq = Mth.square(player.entityInteractionRange());
              if (living != player && living != targetLiving && !player.isAlliedTo(living) && (!(living instanceof ArmorStand armorStand) || !armorStand.isMarker()) && player.distanceToSqr(living) < entityReachSq) {
                living.knockback(0.4f, Mth.sin(player.getYRot() * TO_RADIAN), -Mth.cos(player.getYRot() * TO_RADIAN));
                living.hurt(player.damageSources().playerAttack(player), sweepDamage);
              }
            }

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
            player.sweepAttack();
          }

          // sync player motion
          if (target instanceof ServerPlayer serverTarget && target.hurtMarked) {
            serverTarget.connection.send(new ClientboundSetEntityMotionPacket(target));
            target.hurtMarked = false;
            target.setDeltaMovement(movement);
          }

          // apply hit effects
          if (critical) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, player.getSoundSource(), 1.0F, 1.0F);
            player.crit(target);
          } else if (fullyCharged) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, player.getSoundSource(), 1.0F, 1.0F);
          } else {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, player.getSoundSource(), 1.0F, 1.0F);
          }
          if (enchantmentDamage > 0.0F) {
            player.magicCrit(target);
          }

          // enchantment post effects
          player.setLastHurtMob(target);
          if (player.level() instanceof ServerLevel serverLevel) {
            EnchantmentHelper.doPostAttackEffects(serverLevel, target, damageSource);
          }

          // handle multipart
          Entity parent = target;
          if (target instanceof PartEntity<?> part) {
            parent = part.getParent();
          }

          // damage the tool
          if (!player.level().isClientSide && !stack.isEmpty() && parent instanceof LivingEntity living) {
            ItemStack copy = stack.copy();
            stack.hurtEnemy(living, player);
            if (stack.isEmpty()) {
              EventHooks.onPlayerDestroyItem(player, copy, hand);
              player.setItemInHand(hand, ItemStack.EMPTY);
            }
          }

          // stats
          if (targetLiving != null) {
            float damageDealt = health - targetLiving.getHealth();
            player.awardStat(Stats.DAMAGE_DEALT, Math.round(damageDealt * 10f));
            // particles
            if (player.level() instanceof ServerLevel server && damageDealt > 2f) {
              server.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.5D), target.getZ(), (int)((double)damageDealt * 0.5D), 0.1D, 0.0D, 0.1D, 0.2D);
            }
          }
          player.causeFoodExhaustion(0.1F);
        } else {
          player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, player.getSoundSource(), 1.0F, 1.0F);
        }
      }
      // apply cooldown
      if (hand == InteractionHand.OFF_HAND) {
        OffhandCooldownTracker.applyCooldown(player, getOffhandAttribute(stack, player, Attributes.ATTACK_SPEED), 20);
      } else {
        player.resetAttackStrengthTicker();
      }
      return true;
    }
    return false;
  }


  /* Damage source creation */

  /** Makes a damage source from the given key */
  public static Holder<DamageType> damageType(RegistryAccess access, ResourceKey<DamageType> key) {
    return access.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
  }

  /** Makes a damage source from the given key */
  public static DamageSource damageSource(RegistryAccess access, ResourceKey<DamageType> key) {
    return new DamageSource(damageType(access, key));
  }

  /** Makes a damage source from the given key */
  public static DamageSource damageSource(Level level, ResourceKey<DamageType> key) {
    return new DamageSource(damageType(level.registryAccess(), key));
  }

  /** Makes a damage source from the given key for direct damage from an entity. */
  public static DamageSource damageSource(ResourceKey<DamageType> key, Entity entity) {
    return new DamageSource(damageType(entity.level().registryAccess(), key), entity);
  }

  /** Makes a damage source from the given key for indirect damage, such as from a projectile. */
  public static DamageSource damageSource(ResourceKey<DamageType> key, Entity direct, @Nullable Entity causing) {
    return new DamageSource(damageType(direct.level().registryAccess(), key), direct, causing);
  }
}
