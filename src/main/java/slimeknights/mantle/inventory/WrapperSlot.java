package slimeknights.mantle.inventory;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Used to wrap the slots inside Modules/Subcontainers
 */
public class WrapperSlot extends Slot {

  public final Slot parent;

  public WrapperSlot(Slot slot) {
    super(slot.inventory, slot.getSlotIndex(), slot.xPos, slot.yPos);
    this.parent = slot;
  }

  @Override
  public void onSlotChange(ItemStack p_75220_1_, ItemStack p_75220_2_) {
    this.parent.onSlotChange(p_75220_1_, p_75220_2_);
  }

  @Override
  public void onSlotChanged() {
    this.parent.onSlotChanged();
  }

  @Override
  public boolean isItemValid(ItemStack stack) {
    return this.parent.isItemValid(stack);
  }

  @Override
  public boolean canTakeStack(PlayerEntity playerIn) {
    return this.parent.canTakeStack(playerIn);
  }

  @Override
  public void putStack(ItemStack stack) {
    this.parent.putStack(stack);
  }

  @Override
  public ItemStack onTake(PlayerEntity playerIn, ItemStack stack) {
    this.parent.onTake(playerIn, stack);

    return stack;
  }

  @Override
  public ItemStack getStack() {
    return this.parent.getStack();
  }

  @Override
  public boolean getHasStack() {
    return this.parent.getHasStack();
  }

  @Override
  public int getSlotStackLimit() {
    return this.parent.getSlotStackLimit();
  }

  @Override
  public int getItemStackLimit(ItemStack stack) {
    return this.parent.getItemStackLimit(stack);
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public Pair<ResourceLocation, ResourceLocation> func_225517_c_() {
    return this.parent.func_225517_c_();
  }

  @Override
  public Slot setBackground(ResourceLocation atlas, ResourceLocation sprite) {
    return this.parent.setBackground(atlas, sprite);
  }

  @Override
  public ItemStack decrStackSize(int amount) {
    return this.parent.decrStackSize(amount);
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public boolean isEnabled() {
    return this.parent.isEnabled();
  }
}
