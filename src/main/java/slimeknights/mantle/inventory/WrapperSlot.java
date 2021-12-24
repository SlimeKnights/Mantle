package slimeknights.mantle.inventory;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
  public void onQuickCraft(ItemStack p_75220_1_, ItemStack p_75220_2_) {
    this.parent.onQuickCraft(p_75220_1_, p_75220_2_);
  }

  @Override
  public void setChanged() {
    this.parent.setChanged();
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return this.parent.mayPlace(stack);
  }

  @Override
  public boolean mayPickup(Player playerIn) {
    return this.parent.mayPickup(playerIn);
  }

  @Override
  public void set(ItemStack stack) {
    this.parent.set(stack);
  }

  @Override
  public ItemStack onTake(Player playerIn, ItemStack stack) {
    this.parent.onTake(playerIn, stack);

    return stack;
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
  public int getMaxStackSize() {
    return this.parent.getMaxStackSize();
  }

  @Override
  public int getMaxStackSize(ItemStack stack) {
    return this.parent.getMaxStackSize(stack);
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
    return this.parent.getNoItemIcon();
  }

  @Override
  public Slot setBackground(ResourceLocation atlas, ResourceLocation sprite) {
    return this.parent.setBackground(atlas, sprite);
  }

  @Override
  public ItemStack remove(int amount) {
    return this.parent.remove(amount);
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public boolean isActive() {
    return this.parent.isActive();
  }
}
