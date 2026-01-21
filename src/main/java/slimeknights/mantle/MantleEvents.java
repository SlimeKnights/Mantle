package slimeknights.mantle;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import slimeknights.mantle.datagen.MantleTags;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/** Handles events for any Mantle driven logic. */
@EventBusSubscriber(modid = Mantle.modId, bus = Bus.FORGE)
public class MantleEvents {
  /* Soulbound */
  /**
   * NBT key for items to preserve their slot in soulbound. Applied to items tagged {@link MantleTags.Items#SOULBOUND}.
   * May be used by dependencies mods in {@link LivingDeathEvent} to make items soulbound for other reasons.
   */
  public static final String SOULBOUND_SLOT = "mantle_soulbound";

  /** Called when the player dies to store the slot to return items into */
  @SubscribeEvent
  static void onLivingDeath(LivingDeathEvent event) {
    // this is the latest we can add slot markers to the items so we can return them to slots
    LivingEntity entity = event.getEntity();
    if (!entity.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && entity instanceof Player player && !(player instanceof FakePlayer)) {
      Inventory inventory = player.getInventory();

      // just iterate the whole inventory, no slot specific behavior
      int totalSize = inventory.getContainerSize();
      for (int i = 0; i < totalSize; i++) {
        ItemStack stack = inventory.getItem(i);
        if (!stack.isEmpty() && stack.is(MantleTags.Items.SOULBOUND)) {
          stack.getOrCreateTag().putInt(SOULBOUND_SLOT, i);
        }
      }
    }
  }

  /** Called when the player dies to store the soulbound items in the original inventory */
  @SubscribeEvent(priority = EventPriority.HIGH)
  static void onPlayerDropItems(LivingDropsEvent event) {
    // only care about real players with keep inventory off
    LivingEntity entity = event.getEntity();
    if (!entity.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && entity instanceof Player player && !(entity instanceof FakePlayer)) {
      Collection<ItemEntity> drops = event.getDrops();
      Iterator<ItemEntity> iter = drops.iterator();
      Inventory inventory = player.getInventory();
      List<ItemEntity> takenSlot = new ArrayList<>();
      while (iter.hasNext()) {
        ItemEntity itemEntity = iter.next();
        ItemStack stack = itemEntity.getItem();
        // find items with our soulbound tag set and move them back into the inventory, will move them over later
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(SOULBOUND_SLOT, Tag.TAG_ANY_NUMERIC)) {
          int slot = tag.getInt(SOULBOUND_SLOT);
          // return the tool to its requested slot if possible, remove from the drops
          if (inventory.getItem(slot).isEmpty()) {
            inventory.setItem(slot, stack);
          } else {
            // hold off on handling items that did not get the requested slot for now
            // want to make sure they don't get in the way of items that have not yet been seen
            takenSlot.add(itemEntity);
          }
          iter.remove();
          // don't clear the tag yet, we need it one last time for player clone
        }
      }
      // handle items that did not get their requested slot last, to ensure they don't take someone else's slot while being added to a default
      for (ItemEntity itemEntity : takenSlot) {
        ItemStack stack = itemEntity.getItem();
        if (!inventory.add(stack)) {
          // last resort, somehow we just cannot put the stack anywhere, so drop it on the ground
          // this should never happen, but better to be safe
          // ditch the soulbound slot tag, to prevent item stacking issues
          CompoundTag tag = stack.getTag();
          if (tag != null) {
            tag.remove(SOULBOUND_SLOT);
            if (tag.isEmpty()) {
              stack.setTag(null);
            }
          }
          drops.add(itemEntity);
        }
      }
    }
  }

  /** Called when the new player is created to fetch the soulbound item from the old */
  @SubscribeEvent(priority = EventPriority.HIGH)
  static void onPlayerClone(PlayerEvent.Clone event) {
    // TODO: same as above, lots of duplicated code with tinkers, move to Mantle
    if (!event.isWasDeath()) {
      return;
    }
    Player original = event.getOriginal();
    Player clone = event.getEntity();
    // inventory already copied
    if (clone.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) || original.isSpectator()) {
      return;
    }
    // find items with the soulbound tag set and move them over
    Inventory originalInv = original.getInventory();
    Inventory cloneInv = clone.getInventory();
    int size = Math.min(originalInv.getContainerSize(), cloneInv.getContainerSize()); // not needed probably, but might as well be safe
    List<ItemStack> takenSlot = new ArrayList<>();
    for(int i = 0; i < size; i++) {
      ItemStack stack = originalInv.getItem(i);
      if (!stack.isEmpty()) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(SOULBOUND_SLOT, Tag.TAG_ANY_NUMERIC)) {
          if (cloneInv.getItem(i).isEmpty()) {
            cloneInv.setItem(i, stack);
          } else {
            takenSlot.add(stack);
          }
          // remove the slot tag, clear the tag if needed
          tag.remove(SOULBOUND_SLOT);
          if (tag.isEmpty()) {
            stack.setTag(null);
          }
        }
      }
    }

    // handle items that did not get their requested slot last, to ensure they don't take someone else's slot while being added to a default
    for (ItemStack stack : takenSlot) {
      if (!cloneInv.add(stack)) {
        // last resort, somehow we just cannot put the stack anywhere, so drop it on the ground
        // this should never happen, but better to be safe
        clone.drop(stack, false);
      }
    }
  }
}
