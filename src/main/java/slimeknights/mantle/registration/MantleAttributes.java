package slimeknights.mantle.registration;

import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.event.enchanting.EnchantedBlockLootEvent;
import net.neoforged.neoforge.event.enchanting.EnchantedEntityLootEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.GetEquipmentDropChanceEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.registration.deferred.AttributeDeferredRegister;
import slimeknights.mantle.registration.holder.DeferredAttribute;

/** Handles all new attributes added by Mantle */
@EventBusSubscriber(modid = Mantle.modId)
public class MantleAttributes {
  private static final AttributeDeferredRegister ATTRIBUTES = new AttributeDeferredRegister(Mantle.modId);

  private MantleAttributes() {}

  public static void init(IEventBus bus) {
    ATTRIBUTES.register(bus);
  }

  // generic
  /** Multiplier for knockback this entity takes. Similar to {@link Attributes#KNOCKBACK_RESISTANCE} but can be used to increase knockback */
  public static final DeferredAttribute<PercentageAttribute> KNOCKBACK_MULTIPLIER = ATTRIBUTES.register("knockback_multiplier").sync().negative().multiplier();
  /** Boost to looting level in loot tables */
  public static final DeferredAttribute<RangedAttribute> LOOTING_BOOST = ATTRIBUTES.register("looting_boost").sync().ranged(0, -255, 255);
  /** Boost to fortune level in loot tables */
  public static final DeferredAttribute<RangedAttribute> FORTUNE_BOOST = ATTRIBUTES.register("fortune_boost").sync().ranged(0, -255, 255);

  // player
  /** Multiplier to amount of gained for players. */
  public static final DeferredAttribute<PercentageAttribute> EXPERIENCE_MULTIPLIER = ATTRIBUTES.register("experience_multiplier").multiplier();
  /** Boost to the percent chance of monster equipment dropping. */
  public static final DeferredAttribute<PercentageAttribute> EQUIPMENT_DROP_CHANCE_BOOST = ATTRIBUTES.register("equipment_drop_chance_boost").sync().percent(0, -1, 1);


  @SubscribeEvent
  static void addAttributes(EntityAttributeModificationEvent event) {
    // general attributes
    addToAll(event, KNOCKBACK_MULTIPLIER);
    addToAll(event, LOOTING_BOOST);
    addToAll(event, FORTUNE_BOOST);
    // player attributes
    event.add(EntityType.PLAYER, EXPERIENCE_MULTIPLIER);
    event.add(EntityType.PLAYER, EQUIPMENT_DROP_CHANCE_BOOST);
  }

  /** Adds an attribute to all entities with the given default value. */
  public static void addToAll(EntityAttributeModificationEvent event, DeferredHolder<Attribute,?> attribute, double defaultValue) {
    for (EntityType<? extends LivingEntity> entity : event.getTypes()) {
      event.add(entity, attribute, defaultValue);
    }
  }

  /** Adds an attribute to all entities with the standard default value */
  public static void addToAll(EntityAttributeModificationEvent event, DeferredHolder<Attribute,?> attribute) {
    addToAll(event, attribute, attribute.value().getDefaultValue());
  }


  /* Implementation */

  /** Called when an entity takes knockback to apply {@link #KNOCKBACK_MULTIPLIER} */
  @SubscribeEvent
  static void onKnockback(LivingKnockBackEvent event) {
    event.setStrength((float) (event.getStrength() * event.getEntity().getAttributeValue(KNOCKBACK_MULTIPLIER)));
  }

  /** Called when a block is broken to boost its XP using {@link #EXPERIENCE_MULTIPLIER}. */
  @SubscribeEvent
  static void blockDrops(BlockDropsEvent event) {
    if (event.getBreaker() instanceof LivingEntity living) {
      event.setDroppedExperience((int) Math.round(event.getDroppedExperience() * living.getAttributeValue(EXPERIENCE_MULTIPLIER)));
    }
  }

  /** Called when a monster is killed to boost its experience using the player's {@link #EXPERIENCE_MULTIPLIER}. */
  @SubscribeEvent
  static void livingExperienceDrop(LivingExperienceDropEvent event) {
    Player player = event.getAttackingPlayer();
    if (player != null) {
      event.setDroppedExperience((int) Math.round(event.getDroppedExperience() * player.getAttributeValue(EXPERIENCE_MULTIPLIER)));
    }
  }

  /** Applies {@link #EQUIPMENT_DROP_CHANCE_BOOST} from the attacker to boost equipment drop rates. */
  @SubscribeEvent
  static void equipmentDropChance(GetEquipmentDropChanceEvent event) {
    if (!event.isPreserved()) {
      Player player = event.getEntity().lastHurtByPlayer;
      if (player != null) {
        event.setChance((float)(event.getChance() + player.getAttributeValue(EQUIPMENT_DROP_CHANCE_BOOST)));
      }
    }
  }

  /** Gets the enchantment level from the given attribute value. */
  private static int getEnchantmentLevel(LivingEntity entity, Holder<Attribute> attribute, int current) {
    double value = entity.getAttributeValue(attribute);
    int level = Mth.floor(value);
    // if we have a partial level, treat it as a random chance to increase the level
    if (value > level && entity.getRandom().nextFloat() < (value - level)) {
      level += 1;
    }
    // ensure the final level is between 0 and 255, valid enchantment range
    return Mth.clamp(current + level, 0, 255);
  }

  /** Applies {@link #FORTUNE_BOOST} from the attacker to boost fortune. */
  @SubscribeEvent
  static void blockEnchantment(EnchantedBlockLootEvent event) {
    if (event.getEnchantment().is(Enchantments.FORTUNE) && event.getEntity() instanceof LivingEntity holder) {
      event.setEnchantmentLevel(getEnchantmentLevel(holder, FORTUNE_BOOST, event.getEnchantmentLevel()));
    }
  }

  /** Applies {@link #LOOTING_BOOST} from the attacker to boost looting. */
  @SubscribeEvent
  static void entityEnchantment(EnchantedEntityLootEvent event) {
    if (event.getEnchantment().is(Enchantments.LOOTING) && event.getAttacker() instanceof LivingEntity attacker) {
      event.setEnchantmentLevel(getEnchantmentLevel(attacker, LOOTING_BOOST, event.getEnchantmentLevel()));
    }
  }
}
