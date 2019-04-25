package slimeknights.mantle.inventory;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Used to wrap the slots inside Modules/Subcontainers
 */
public class SlotWrapper extends Slot {

  public final Slot parent;

  public SlotWrapper(Slot slot) {
    super(slot.inventory, slot.getSlotIndex(), slot.xPos, slot.yPos);
    this.parent = slot;
  }

  @Override
  public void onSlotChange(ItemStack p_75220_1_, ItemStack p_75220_2_) {
    parent.onSlotChange(p_75220_1_, p_75220_2_);
  }

  @Override
  public void onSlotChanged() {
    parent.onSlotChanged();
  }

  @Override
  public boolean isItemValid(ItemStack stack) {
    return parent.isItemValid(stack);
  }

  @Override
  public boolean canTakeStack(EntityPlayer playerIn) {
    return parent.canTakeStack(playerIn);
  }

  @Override
  public void putStack(@Nonnull ItemStack stack) {
    parent.putStack(stack);
  }

  @Override
  @Nonnull
  public ItemStack onTake(EntityPlayer playerIn, ItemStack stack) {
    parent.onTake(playerIn, stack);

    return stack;
  }

  @Override
  @Nonnull
  public ItemStack getStack() {
    return parent.getStack();
  }

  @Override
  public boolean getHasStack() {
    return parent.getHasStack();
  }

  @Override
  public int getSlotStackLimit() {
    return parent.getSlotStackLimit();
  }

  @Override
  public int getItemStackLimit(ItemStack stack) {
    return parent.getItemStackLimit(stack);
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public String getSlotTexture() {
    return parent.getSlotTexture();
  }

  @Nonnull
  @Override
  public ItemStack decrStackSize(int amount) {
    return parent.decrStackSize(amount);
  }

  @Override
  public boolean isHere(IInventory inv, int slotIn) {
    return parent.isHere(inv, slotIn);
  }

  @Nonnull
  @Override
  public ResourceLocation getBackgroundLocation() {
    return parent.getBackgroundLocation();
  }

  @Override
  public void setBackgroundName(@Nonnull String name) {
    parent.setBackgroundName(name);
  }

  @Nonnull
  @Override
  public TextureAtlasSprite getBackgroundSprite() {
    return parent.getBackgroundSprite();
  }

  @Override
  public void setBackgroundLocation(@Nonnull ResourceLocation texture) {
    parent.setBackgroundLocation(texture);
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public boolean isEnabled() {
    return parent.isEnabled();
  }
}
