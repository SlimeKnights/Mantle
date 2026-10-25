package slimeknights.mantle.registration;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
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
  public static final DeferredAttribute<PercentageAttribute> KNOCKBACK_MULTIPLIER = ATTRIBUTES.register("generic.knockback_multiplier").sync().negative().multiplier();

  // player
  /** Multiplier to amount of gained for players. */
  public static final DeferredAttribute<PercentageAttribute> EXPERIENCE_MULTIPLIER = ATTRIBUTES.register("player.experience_multiplier").multiplier();


  @SubscribeEvent
  static void addAttributes(EntityAttributeModificationEvent event) {
    // general attributes
    addToAll(event, KNOCKBACK_MULTIPLIER);
    // player attributes
    event.add(EntityType.PLAYER, EXPERIENCE_MULTIPLIER);
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
}
