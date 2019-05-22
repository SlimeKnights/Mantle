package slimeknights.mantle.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.INameable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import slimeknights.mantle.util.ItemStackList;

// Updated version of InventoryLogic in Mantle. Also contains a few bugfixes
public class TileInventory extends MantleTileEntity implements IInventory {

  private NonNullList<ItemStack> inventory;
  protected ITextComponent inventoryTitle;
  protected boolean hasCustomName;
  protected int stackSizeLimit;
  protected IItemHandlerModifiable itemHandler;
  protected LazyOptional<IItemHandlerModifiable> itemHandlerCap;

  /**
   * @param name Localization String for the inventory title. Can be overridden through setCustomName
   */
  public TileInventory(TileEntityType<?> tileEntityTypeIn, ITextComponent name, int inventorySize) {
    this(tileEntityTypeIn, name, inventorySize, 64);
  }

  /**
   * @param name Localization String for the inventory title. Can be overridden through setCustomName
   */
  public TileInventory(TileEntityType<?> tileEntityTypeIn, ITextComponent name, int inventorySize, int maxStackSize) {
    super(tileEntityTypeIn);
    this.inventory = NonNullList.<ItemStack> withSize(inventorySize, ItemStack.EMPTY);
    this.stackSizeLimit = maxStackSize;
    this.inventoryTitle = name;
    this.itemHandler = new InvWrapper(this);
    this.itemHandlerCap = LazyOptional.of(() -> this.itemHandler);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
    if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
      return itemHandlerCap.cast();
    }
    return super.getCapability(capability, facing);
  }

  public IItemHandlerModifiable getItemHandler() {
    return itemHandler;
  }

  /* Inventory management */

  @Nonnull
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

  /** Same as resize, but does not call markDirty. Used on loading from NBT */
  private void resizeInternal(int size) {
    // save effort if the size did not change
    if(size == inventory.size()) {
      return;
    }
    ItemStackList newInventory = ItemStackList.withSize(size);

    for (int i = 0; i < size && i < inventory.size(); i++) {
      newInventory.set(i, inventory.get(i));
    }
    inventory = newInventory;
  }

  public void resize(int size) {
    resizeInternal(size);
    this.markDirtyFast();
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
  public void setInventorySlotContents(int slot, @Nonnull ItemStack itemstack) {
    if(slot < 0 || slot >= inventory.size()) {
      return;
    }

    ItemStack current = inventory.get(slot);
    inventory.set(slot, itemstack);

    if(!itemstack.isEmpty() && itemstack.getCount() > getInventoryStackLimit()) {
      itemstack.setCount(getInventoryStackLimit());
    }
    if(!ItemStack.areItemStacksEqual(current, itemstack)) {
      this.markDirtyFast();
    }
  }

  @Nonnull
  @Override
  public ItemStack decrStackSize(int slot, int quantity) {
    if(quantity <= 0) {
      return ItemStack.EMPTY;
    }
    ItemStack itemStack = getStackInSlot(slot);

    if(itemStack.isEmpty()) {
      return ItemStack.EMPTY;
    }

    // whole itemstack taken out
    if(itemStack.getCount() <= quantity) {
      setInventorySlotContents(slot, ItemStack.EMPTY);
      this.markDirtyFast();
      return itemStack;
    }

    // split itemstack
    itemStack = itemStack.split(quantity);
    // slot is empty, set to ItemStack.EMPTY
    // isn't this redundant to the above check?
    if(getStackInSlot(slot).getCount() == 0) {
      setInventorySlotContents(slot, ItemStack.EMPTY);
    }

    this.markDirtyFast();
    // return remainder
    return itemStack;
  }

  @Nonnull
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
  public ITextComponent getName() {
    return this.inventoryTitle;
  }

  @Override
  public boolean hasCustomName() {
    return this.hasCustomName;
  }

  public void setCustomName(ITextComponent customName) {
    this.hasCustomName = true;
    this.inventoryTitle = customName;
  }

  @Nullable
  @Override
  public ITextComponent getCustomName() {
    return this.inventoryTitle;
  }

  @Nonnull
  @Override
  public ITextComponent getDisplayName() {
    if(hasCustomName()) {
      return new TextComponentString(getName().getFormattedText());
    }

    return new TextComponentTranslation(getName().getFormattedText());
  }


  /* Supporting methods */
  @Override
  public boolean isUsableByPlayer(@Nonnull EntityPlayer entityplayer) {
    // block changed/got broken?
    if(world.getTileEntity(pos) != this || world.getBlockState(pos).getBlock() == Blocks.AIR) {
      return false;
    }

    return
        entityplayer.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)
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
  public void read(NBTTagCompound tags) {
    super.read(tags);
    this.resizeInternal(tags.getInt("InventorySize"));

    readInventoryFromNBT(tags);

    if(tags.contains("CustomName", 8)) {
      this.inventoryTitle = ITextComponent.Serializer.fromJson(tags.getString("CustomName"));
    }
  }

  @Nonnull
  @Override
  public NBTTagCompound write(NBTTagCompound tags) {
    super.write(tags);

    tags.putInt("InventorySize", inventory.size());

    writeInventoryToNBT(tags);

    if(this.hasCustomName()) {
      tags.putString("CustomName", ITextComponent.Serializer.toJson(this.inventoryTitle));
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
        itemTag.putByte("Slot", (byte) i);
        inventory.getStackInSlot(i).write(itemTag);
        nbttaglist.add(itemTag);
      }
    }

    tag.put("Items", nbttaglist);
  }

  /** Reads a an inventory from the tag. Overwrites current content */
  public void readInventoryFromNBT(NBTTagCompound tag) {
    NBTTagList nbttaglist = tag.getList("Items", 10);

    int limit = getInventoryStackLimit();
    ItemStack stack;
    for(int i = 0; i < nbttaglist.size(); ++i) {
      NBTTagCompound itemTag = nbttaglist.getCompound(i);
      int slot = itemTag.getByte("Slot") & 255;

      if(slot >= 0 && slot < inventory.size()) {
        stack = ItemStack.read(itemTag);
        if(!stack.isEmpty() && stack.getCount() > limit) {
          stack.setCount(limit);
        }
        inventory.set(slot, stack);
      }
    }
  }

  /* Default implementations of hardly used methods */
  @Nonnull
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
  public boolean isEmpty() {
    for (ItemStack itemstack : this.inventory) {
      if (!itemstack.isEmpty()) {
        return false;
      }
    }

    return true;
  }
}
