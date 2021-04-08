package slimeknights.mantle.inventory;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Used to wrap the slots inside Modules/Subcontainers
 */
public class WrapperSlot extends Slot {

  public final Slot parent;

  public WrapperSlot(Slot slot) {
    super(slot.inventory, slot.getSlotIndex(), slot.x, slot.y);
    this.parent = slot;
  }

  @Override
  public void onStackChanged(ItemStack p_75220_1_, ItemStack p_75220_2_) {
    this.parent.onStackChanged(p_75220_1_, p_75220_2_);
  }

  @Override
  public void markDirty() {
    this.parent.markDirty();
  }

  @Override
  public boolean canInsert(ItemStack stack) {
    return this.parent.canInsert(stack);
  }

  @Override
  public boolean canTakeItems(PlayerEntity playerIn) {
    return this.parent.canTakeItems(playerIn);
  }

  @Override
  public void setStack(ItemStack stack) {
    this.parent.setStack(stack);
  }

  @Override
  public ItemStack onTakeItem(PlayerEntity playerIn, ItemStack stack) {
    this.parent.onTakeItem(playerIn, stack);

    return stack;
  }

  @Override
  public ItemStack getStack() {
    return this.parent.getStack();
  }

  @Override
  public boolean hasStack() {
    return this.parent.hasStack();
  }

  @Override
  public int getMaxItemCount() {
    return this.parent.getMaxItemCount();
  }

  @Override
  public int getMaxItemCount(ItemStack stack) {
    return this.parent.getMaxItemCount(stack);
  }

  @Override
  @Environment(EnvType.CLIENT)
  public Pair<Identifier, Identifier> getBackgroundSprite() {
    return this.parent.getBackgroundSprite();
  }

  @Override
  public Slot setBackground(Identifier atlas, Identifier sprite) {
    return this.parent.setBackground(atlas, sprite);
  }

  @Override
  public ItemStack takeStack(int amount) {
    return this.parent.takeStack(amount);
  }

  @Environment(EnvType.CLIENT)
  @Override
  public boolean doDrawHoveringEffect() {
    return this.parent.doDrawHoveringEffect();
  }
}
