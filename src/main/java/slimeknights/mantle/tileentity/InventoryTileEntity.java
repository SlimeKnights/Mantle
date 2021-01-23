package slimeknights.mantle.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.INameable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import slimeknights.mantle.util.ItemStackList;

import javax.annotation.Nullable;

// Updated version of InventoryLogic in Mantle. Also contains a few bugfixes DOES NOT OVERRIDE createMenu
public abstract class InventoryTileEntity extends NamableTileEntity implements IInventory, INamedContainerProvider, INameable {
  private static final String TAG_INVENTORY_SIZE = "InventorySize";
  private static final String TAG_ITEMS = "Items";
  private static final String TAG_SLOT = "Slot";

  private NonNullList<ItemStack> inventory;
  protected int stackSizeLimit;
  protected IItemHandlerModifiable itemHandler;
  protected LazyOptional<IItemHandlerModifiable> itemHandlerCap;
  /** @deprecated Use {@link #getName()}, {@link #getDefaultName()}, or {@link #getCustomName()} */
  @Deprecated
  protected ITextComponent inventoryTitle;

  /**
   * @param name Localization String for the inventory title. Can be overridden through setCustomName
   */
  public InventoryTileEntity(TileEntityType<?> tileEntityTypeIn, ITextComponent name, int inventorySize) {
    this(tileEntityTypeIn, name, inventorySize, 64);
  }

  /**
   * @param name Localization String for the inventory title. Can be overridden through setCustomName
   */
  public InventoryTileEntity(TileEntityType<?> tileEntityTypeIn, ITextComponent name, int inventorySize, int maxStackSize) {
    super(tileEntityTypeIn, name);
    this.inventory = NonNullList.withSize(inventorySize, ItemStack.EMPTY);
    this.stackSizeLimit = maxStackSize;
    this.itemHandler = new InvWrapper(this);
    this.itemHandlerCap = LazyOptional.of(() -> this.itemHandler);
    this.inventoryTitle = name;
  }

  @Override
  public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
    if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
      return this.itemHandlerCap.cast();
    }
    return super.getCapability(capability, facing);
  }

  @Override
  protected void invalidateCaps() {
    super.invalidateCaps();
    this.itemHandlerCap.invalidate();
  }

  public IItemHandlerModifiable getItemHandler() {
    return this.itemHandler;
  }

  /* Inventory management */

  @Override
  public ItemStack getStackInSlot(int slot) {
    if (slot < 0 || slot >= this.inventory.size()) {
      return ItemStack.EMPTY;
    }

    return this.inventory.get(slot);
  }

  public boolean isStackInSlot(int slot) {
    return !this.getStackInSlot(slot).isEmpty();
  }

  /**
   * Same as resize, but does not call markDirty. Used on loading from NBT
   */
  private void resizeInternal(int size) {
    // save effort if the size did not change
    if (size == this.inventory.size()) {
      return;
    }
    ItemStackList newInventory = ItemStackList.withSize(size);

    for (int i = 0; i < size && i < this.inventory.size(); i++) {
      newInventory.set(i, this.inventory.get(i));
    }
    this.inventory = newInventory;
  }

  public void resize(int size) {
    this.resizeInternal(size);
    this.markDirtyFast();
  }

  @Override
  public int getSizeInventory() {
    return this.inventory.size();
  }

  @Override
  public int getInventoryStackLimit() {
    return this.stackSizeLimit;
  }

  @Override
  public void setInventorySlotContents(int slot, ItemStack itemstack) {
    if (slot < 0 || slot >= this.inventory.size()) {
      return;
    }

    ItemStack current = this.inventory.get(slot);
    this.inventory.set(slot, itemstack);

    if (!itemstack.isEmpty() && itemstack.getCount() > this.getInventoryStackLimit()) {
      itemstack.setCount(this.getInventoryStackLimit());
    }
    if (!ItemStack.areItemStacksEqual(current, itemstack)) {
      this.markDirtyFast();
    }
  }

  @Override
  public ItemStack decrStackSize(int slot, int quantity) {
    if (quantity <= 0) {
      return ItemStack.EMPTY;
    }
    ItemStack itemStack = this.getStackInSlot(slot);

    if (itemStack.isEmpty()) {
      return ItemStack.EMPTY;
    }

    // whole itemstack taken out
    if (itemStack.getCount() <= quantity) {
      this.setInventorySlotContents(slot, ItemStack.EMPTY);
      this.markDirtyFast();
      return itemStack;
    }

    // split itemstack
    itemStack = itemStack.split(quantity);
    // slot is empty, set to ItemStack.EMPTY
    // isn't this redundant to the above check?
    if (this.getStackInSlot(slot).getCount() == 0) {
      this.setInventorySlotContents(slot, ItemStack.EMPTY);
    }

    this.markDirtyFast();
    // return remainder
    return itemStack;
  }

  @Override
  public ItemStack removeStackFromSlot(int slot) {
    ItemStack itemStack = this.getStackInSlot(slot);
    this.setInventorySlotContents(slot, ItemStack.EMPTY);
    return itemStack;
  }

  @Override
  public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
    if (slot < this.getSizeInventory()) {
      return this.inventory.get(slot).isEmpty() || itemstack.getCount() + this.inventory.get(slot).getCount() <= this.getInventoryStackLimit();
    }
    return false;
  }

  @Override
  public void clear() {
    for (int i = 0; i < this.inventory.size(); i++) {
      this.inventory.set(i, ItemStack.EMPTY);
    }
  }

  @Override
  public void setCustomName(ITextComponent customName) {
    super.setCustomName(customName);
    this.inventoryTitle = customName;
  }

  /* Supporting methods */
  @Override
  public boolean isUsableByPlayer(PlayerEntity entityplayer) {
    // block changed/got broken?
    if (world == null || this.world.getTileEntity(this.pos) != this || this.getBlockState().getBlock() == Blocks.AIR) {
      return false;
    }

    return entityplayer.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64D;
  }

  @Override
  public void openInventory(PlayerEntity player) {}

  @Override
  public void closeInventory(PlayerEntity player) {}

  /* NBT */
  @Override
  public void read(BlockState blockState, CompoundNBT tags) {
    super.read(blockState, tags);
    this.resizeInternal(tags.getInt(TAG_INVENTORY_SIZE));
    this.readInventoryFromNBT(tags);
    this.inventoryTitle = getName();
  }

  @Override
  public void writeSynced(CompoundNBT tags) {
    super.writeSynced(tags);
    // only sync the size to the client by default
    tags.putInt(TAG_INVENTORY_SIZE, this.inventory.size());
  }
  
  @Override
  public CompoundNBT write(CompoundNBT tags) {
    super.write(tags);
    this.writeInventoryToNBT(tags);
    return tags;
  }

  /**
   * Writes the contents of the inventory to the tag
   */
  public void writeInventoryToNBT(CompoundNBT tag) {
    IInventory inventory = this;
    ListNBT nbttaglist = new ListNBT();

    for (int i = 0; i < inventory.getSizeInventory(); i++) {
      if (!inventory.getStackInSlot(i).isEmpty()) {
        CompoundNBT itemTag = new CompoundNBT();
        itemTag.putByte(TAG_SLOT, (byte) i);
        inventory.getStackInSlot(i).write(itemTag);
        nbttaglist.add(itemTag);
      }
    }

    tag.put(TAG_ITEMS, nbttaglist);
  }

  /**
   * Reads a an inventory from the tag. Overwrites current content
   */
  public void readInventoryFromNBT(CompoundNBT tag) {
    ListNBT nbttaglist = tag.getList(TAG_ITEMS, NBT.TAG_COMPOUND);

    int limit = this.getInventoryStackLimit();
    ItemStack stack;
    for (int i = 0; i < nbttaglist.size(); ++i) {
      CompoundNBT itemTag = nbttaglist.getCompound(i);
      int slot = itemTag.getByte(TAG_SLOT) & 255;
      if (slot < this.inventory.size()) {
        stack = ItemStack.read(itemTag);
        if (!stack.isEmpty() && stack.getCount() > limit) {
          stack.setCount(limit);
        }
        this.inventory.set(slot, stack);
      }
    }
  }

  /* Default implementations of hardly used methods */
  @Deprecated
  public ItemStack getStackInSlotOnClosing(int slot) {
    return ItemStack.EMPTY;
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
