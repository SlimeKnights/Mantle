package slimeknights.mantle.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.Arrays;

import javax.annotation.Nonnull;

import slimeknights.mantle.util.ItemStackList;

// Updated version of InventoryLogic in Mantle. Also contains a few bugfixes
public class TileInventory extends MantleTileEntity implements IInventory {

  private NonNullList<ItemStack> inventory;
  protected String inventoryTitle;
  protected boolean hasCustomName;
  protected int stackSizeLimit;
  protected IItemHandlerModifiable itemHandler;

  /**
   * @param name Localization String for the inventory title. Can be overridden through setCustomName
   */
  public TileInventory(String name, int inventorySize) {
    this(name, inventorySize, 64);
  }

  /**
   * @param name Localization String for the inventory title. Can be overridden through setCustomName
   */
  public TileInventory(String name, int inventorySize, int maxStackSize) {
    this.inventory = NonNullList.<ItemStack> withSize(inventorySize, ItemStack.EMPTY);
    this.stackSizeLimit = maxStackSize;
    this.inventoryTitle = name;
    this.itemHandler = new InvWrapper(this);
  }

  @Override
  public boolean hasCapability(@Nonnull Capability<?> capability, @Nonnull EnumFacing facing) {
    return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
  }

  @Nonnull
  @Override
  public <T> T getCapability(@Nonnull Capability<T> capability, @Nonnull EnumFacing facing) {
    if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
      return (T) itemHandler;
    }
    return super.getCapability(capability, facing);
  }

  public IItemHandlerModifiable getItemHandler() {
    return itemHandler;
  }

  /* Inventory management */

  @Override
  public ItemStack getStackInSlot(int slot) {
    if(slot < 0 || slot >= inventory.size()) {
      return ItemStack.EMPTY;
    }

    return inventory.get(slot);
  }

  public boolean isStackInSlot(int slot) {
    return !getStackInSlot(slot).isEmpty();
  }

  public void resize(int size) {
    ItemStackList newInventory = ItemStackList.withSize(size);

    for (int i = 0; i < size && i < inventory.size(); i++) {
      newInventory.set(i, inventory.get(i));
    }
    inventory = newInventory;
  }

  @Override
  public int getSizeInventory() {
    return inventory.size();
  }

  @Override
  public int getInventoryStackLimit() {
    return stackSizeLimit;
  }

  @Override
  public void setInventorySlotContents(int slot, ItemStack itemstack) {
    if(slot < 0 || slot >= inventory.size()) {
      return;
    }

    inventory.set(slot, itemstack);

    if(!itemstack.isEmpty() && itemstack.getCount() > getInventoryStackLimit()) {
      itemstack.setCount(getInventoryStackLimit());
    }
  }

  @Override
  public ItemStack decrStackSize(int slot, int quantity) {
    ItemStack itemStack = getStackInSlot(slot);

    if(itemStack.isEmpty()) {
      return ItemStack.EMPTY;
    }

    // whole itemstack taken out
    if(itemStack.getCount() <= quantity) {
      setInventorySlotContents(slot, ItemStack.EMPTY);
      this.markDirty();
      return itemStack;
    }

    // split itemstack
    itemStack = itemStack.splitStack(quantity);
    // slot is empty, set to ItemStack.EMPTY
    if(getStackInSlot(slot).getCount() == 0) {
      setInventorySlotContents(slot, ItemStack.EMPTY);
    }

    this.markDirty();
    // return remainder
    return itemStack;
  }

  @Override
  public ItemStack removeStackFromSlot(int slot) {
    ItemStack itemStack = getStackInSlot(slot);
    setInventorySlotContents(slot, ItemStack.EMPTY);
    return itemStack;
  }

  @Override
  public boolean isItemValidForSlot(int slot, @Nonnull ItemStack itemstack) {
    if(slot < getSizeInventory()) {
      if(inventory.get(slot).isEmpty() || itemstack.getCount() + inventory.get(slot).getCount() <= getInventoryStackLimit()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void clear() {
    for(int i = 0; i < inventory.size(); i++) {
      inventory.set(i, ItemStack.EMPTY);
    }
  }

  @Nonnull
  @Override
  public String getName() {
    return this.inventoryTitle;
  }

  @Override
  public boolean hasCustomName() {
    return this.hasCustomName;
  }

  public void setCustomName(String customName) {
    this.hasCustomName = true;
    this.inventoryTitle = customName;
  }

  @Nonnull
  @Override
  public ITextComponent getDisplayName() {
    if(hasCustomName()) {
      return new TextComponentString(getName());
    }

    return new TextComponentTranslation(getName());
  }


  /* Supporting methods */
  @Override
  public boolean isUsableByPlayer(@Nonnull EntityPlayer entityplayer) {
    // block changed/got broken?
    if(world.getTileEntity(pos) != this || world.getBlockState(pos).getBlock() == Blocks.AIR) {
      return false;
    }

    return
        entityplayer.getDistanceSq((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D)
        <= 64D;
  }

  @Override
  public void openInventory(@Nonnull EntityPlayer player) {

  }

  @Override
  public void closeInventory(@Nonnull EntityPlayer player) {

  }

  /* NBT */
  @Override
  public void readFromNBT(NBTTagCompound tags) {
    super.readFromNBT(tags);
    this.resize(tags.getInteger("InventorySize"));

    readInventoryFromNBT(tags);

    if(tags.hasKey("CustomName", 8)) {
      this.inventoryTitle = tags.getString("CustomName");
    }
  }

  @Nonnull
  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound tags) {
    super.writeToNBT(tags);

    tags.setInteger("InventorySize", inventory.size());

    writeInventoryToNBT(tags);

    if(this.hasCustomName()) {
      tags.setString("CustomName", this.inventoryTitle);
    }
    return tags;
  }

  /** Writes the contents of the inventory to the tag */
  public void writeInventoryToNBT(NBTTagCompound tag) {
    IInventory inventory = this;
    NBTTagList nbttaglist = new NBTTagList();

    for(int i = 0; i < inventory.getSizeInventory(); i++) {
      if(!inventory.getStackInSlot(i).isEmpty()) {
        NBTTagCompound itemTag = new NBTTagCompound();
        itemTag.setByte("Slot", (byte) i);
        inventory.getStackInSlot(i).writeToNBT(itemTag);
        nbttaglist.appendTag(itemTag);
      }
    }

    tag.setTag("Items", nbttaglist);
  }

  /** Reads a an inventory from the tag. Overwrites current content */
  public void readInventoryFromNBT(NBTTagCompound tag) {
    IInventory inventory = this;
    NBTTagList nbttaglist = tag.getTagList("Items", 10);

    for(int i = 0; i < nbttaglist.tagCount(); ++i) {
      NBTTagCompound itemTag = nbttaglist.getCompoundTagAt(i);
      int slot = itemTag.getByte("Slot") & 255;

      if(slot >= 0 && slot < inventory.getSizeInventory()) {
        inventory.setInventorySlotContents(slot, new ItemStack(itemTag));
      }
    }
  }

  /* Default implementations of hardly used methods */
  public ItemStack getStackInSlotOnClosing(int slot) {
    return ItemStack.EMPTY;
  }

  @Override
  public int getField(int id) {
    return 0;
  }

  @Override
  public void setField(int id, int value) {

  }

  @Override
  public int getFieldCount() {
    return 0;
  }

  @Override
  public boolean isEmpty()
  {
    for (ItemStack itemstack : this.inventory)
    {
      if (!itemstack.isEmpty())
      {
        return false;
      }
    }

    return true;
  }
}
