package slimeknights.mantle.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.Arrays;

// Updated version of InventoryLogic in Mantle. Also contains a few bugfixes
public class TileInventory extends TileEntity implements IInventory {

  private ItemStack[] inventory;
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
    this.inventory = new ItemStack[inventorySize];
    this.stackSizeLimit = maxStackSize;
    this.inventoryTitle = name;
    this.itemHandler = new InvWrapper(this);
  }

  @Override
  public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
    return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
  }

  @Override
  public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
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
    if(slot < 0 || slot >= inventory.length) {
      return null;
    }

    return inventory[slot];
  }

  public boolean isStackInSlot(int slot) {
    return getStackInSlot(slot) != null;
  }

  public void resize(int size) {
    inventory = Arrays.copyOf(inventory, size);
  }

  @Override
  public int getSizeInventory() {
    return inventory.length;
  }

  @Override
  public int getInventoryStackLimit() {
    return stackSizeLimit;
  }

  @Override
  public void setInventorySlotContents(int slot, ItemStack itemstack) {
    if(slot < 0 || slot >= inventory.length) {
      return;
    }

    inventory[slot] = itemstack;
    if(itemstack != null && itemstack.stackSize > getInventoryStackLimit()) {
      itemstack.stackSize = getInventoryStackLimit();
    }
  }

  @Override
  public ItemStack decrStackSize(int slot, int quantity) {
    ItemStack itemStack = getStackInSlot(slot);

    if(itemStack == null) {
      return null;
    }

    // whole itemstack taken out
    if(itemStack.stackSize <= quantity) {
      setInventorySlotContents(slot, null);
      this.markDirty();
      return itemStack;
    }

    // split itemstack
    itemStack = itemStack.splitStack(quantity);
    // slot is empty, set to null
    if(getStackInSlot(slot).stackSize == 0) {
      setInventorySlotContents(slot, null);
    }

    this.markDirty();
    // return remainder
    return itemStack;
  }

  @Override
  public ItemStack removeStackFromSlot(int slot) {
    ItemStack itemStack = getStackInSlot(slot);
    setInventorySlotContents(slot, null);
    return itemStack;
  }

  @Override
  public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
    if(slot < getSizeInventory()) {
      if(inventory[slot] == null || itemstack.stackSize + inventory[slot].stackSize <= getInventoryStackLimit()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void clear() {
    for(int i = 0; i < inventory.length; i++) {
      inventory[i] = null;
    }
  }

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

  @Override
  public ITextComponent getDisplayName() {
    if(hasCustomName()) {
      return new TextComponentString(getName());
    }

    return new TextComponentTranslation(getName());
  }


  /* Supporting methods */
  @Override
  public boolean isUseableByPlayer(EntityPlayer entityplayer) {
    // block changed/got broken?
    if(worldObj.getTileEntity(pos) != this || worldObj.getBlockState(pos).getBlock() == Blocks.AIR) {
      return false;
    }

    return
        entityplayer.getDistanceSq((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D)
        <= 64D;
  }

  @Override
  public void openInventory(EntityPlayer player) {

  }

  @Override
  public void closeInventory(EntityPlayer player) {

  }

  /* NBT */
  @Override
  public void readFromNBT(NBTTagCompound tags) {
    super.readFromNBT(tags);
    readInventoryFromNBT(tags);
  }

  public void readInventoryFromNBT(NBTTagCompound tags) {
    super.readFromNBT(tags);

    this.resize(tags.getInteger("InventorySize"));

    readInventoryFromNBT(this, tags);

    if(tags.hasKey("CustomName", 8)) {
      this.inventoryTitle = tags.getString("CustomName");
    }
  }

  @Override
  public void writeToNBT(NBTTagCompound tags) {
    super.writeToNBT(tags);

    tags.setInteger("InventorySize", inventory.length);

    writeInventoryToNBT(this, tags);

    if(this.hasCustomName()) {
      tags.setString("CustomName", this.inventoryTitle);
    }
  }

  /** Writes the contents of the inventory to the tag */
  public static void writeInventoryToNBT(IInventory inventory, NBTTagCompound tag) {
    NBTTagList nbttaglist = new NBTTagList();

    for(int i = 0; i < inventory.getSizeInventory(); i++) {
      if(inventory.getStackInSlot(i) != null) {
        NBTTagCompound itemTag = new NBTTagCompound();
        itemTag.setByte("Slot", (byte) i);
        inventory.getStackInSlot(i).writeToNBT(itemTag);
        nbttaglist.appendTag(itemTag);
      }
    }

    tag.setTag("Items", nbttaglist);
  }

  /** Reads a an inventory from the tag. Overwrites current content */
  public static void readInventoryFromNBT(IInventory inventory, NBTTagCompound tag) {
    NBTTagList nbttaglist = tag.getTagList("Items", 10);

    for(int i = 0; i < nbttaglist.tagCount(); ++i) {
      NBTTagCompound itemTag = nbttaglist.getCompoundTagAt(i);
      int slot = itemTag.getByte("Slot") & 255;

      if(slot >= 0 && slot < inventory.getSizeInventory()) {
        inventory.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(itemTag));
      }
    }
  }

  /* Default implementations of hardly used methods */
  public ItemStack getStackInSlotOnClosing(int slot) {
    return null;
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
}
