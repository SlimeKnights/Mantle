package slimeknights.mantle.inventory;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Used to wrap the slots inside Modules/Subcontainers
 */
public class WrapperSlot extends Slot {

  public final Slot parent;

  public WrapperSlot(Slot slot) {
    super(slot.container, slot.getSlotIndex(), slot.x, slot.y);
    this.parent = slot;
  }

  @Override
  public void onQuickCraft(ItemStack oldStack, ItemStack newStack) {
    this.parent.onQuickCraft(oldStack, newStack);
  }

  @Override
  public void onTake(Player playerIn, ItemStack stack) {
    this.parent.onTake(playerIn, stack);
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return this.parent.mayPlace(stack);
  }

  @Override
  public ItemStack getItem() {
    return this.parent.getItem();
  }

  @Override
  public boolean hasItem() {
    return this.parent.hasItem();
  }

  @Override
  public void set(ItemStack stack) {
    this.parent.set(stack);
  }

  @Override
  public void initialize(ItemStack stack) {
    this.parent.initialize(stack);
  }

  @Override
  public void setChanged() {
    this.parent.setChanged();
  }

  @Override
  public int getMaxStackSize() {
    return this.parent.getMaxStackSize();
  }

  @Override
  public int getMaxStackSize(ItemStack stack) {
    return this.parent.getMaxStackSize(stack);
  }

  @Override
  public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
    return this.parent.getNoItemIcon();
  }

  @Override
  public ItemStack remove(int amount) {
    return this.parent.remove(amount);
  }

  @Override
  public boolean mayPickup(Player playerIn) {
    return this.parent.mayPickup(playerIn);
  }

  @Override
  public boolean isActive() {
    return this.parent.isActive();
  }

  @Override
  public Slot setBackground(ResourceLocation atlas, ResourceLocation sprite) {
    return this.parent.setBackground(atlas, sprite);
  }

  @Override
  public Optional<ItemStack> tryRemove(int pCount, int pDecrement, Player pPlayer) {
    return this.parent.tryRemove(pCount, pDecrement, pPlayer);
  }

  @Override
  public ItemStack safeTake(int pCount, int pDecrement, Player pPlayer) {
    return this.parent.safeTake(pCount, pDecrement, pPlayer);
  }

  @Override
  public ItemStack safeInsert(ItemStack pStack, int pIncrement) {
    return this.parent.safeInsert(pStack, pIncrement);
  }

  @Override
  public boolean allowModification(Player pPlayer) {
    return this.parent.allowModification(pPlayer);
  }
}
