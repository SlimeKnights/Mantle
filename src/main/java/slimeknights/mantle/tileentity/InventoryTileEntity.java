package slimeknights.mantle.tileentity;

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
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import slimeknights.mantle.util.ItemStackList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// Updated version of InventoryLogic in Mantle. Also contains a few bugfixes DOES NOT OVERRIDE createMenu
public abstract class InventoryTileEntity extends MantleTileEntity implements IInventory, INamedContainerProvider, INameable {

  private NonNullList<ItemStack> inventory;
  protected ITextComponent inventoryTitle;
  protected boolean hasCustomName;
  protected int stackSizeLimit;
  protected IItemHandlerModifiable itemHandler;
  protected LazyOptional<IItemHandlerModifiable> itemHandlerCap;

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
    super(tileEntityTypeIn);
    this.inventory = NonNullList.withSize(inventorySize, ItemStack.EMPTY);
    this.stackSizeLimit = maxStackSize;
    this.inventoryTitle = name;
    this.itemHandler = new InvWrapper(this);
    this.itemHandlerCap = LazyOptional.of(() -> this.itemHandler);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
    if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
      return this.itemHandlerCap.cast();
    }
    return super.getCapability(capability, facing);
  }

  public IItemHandlerModifiable getItemHandler() {
    return this.itemHandler;
  }

  /* Inventory management */

  @Nonnull
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

  /** Same as resize, but does not call markDirty. Used on loading from NBT */
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
  public void setInventorySlotContents(int slot, @Nonnull ItemStack itemstack) {
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

  @Nonnull
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

  @Nonnull
  @Override
  public ItemStack removeStackFromSlot(int slot) {
    ItemStack itemStack = this.getStackInSlot(slot);
    this.setInventorySlotContents(slot, ItemStack.EMPTY);
    return itemStack;
  }

  @Override
  public boolean isItemValidForSlot(int slot, @Nonnull ItemStack itemstack) {
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
    if (this.hasCustomName()) {
      return new StringTextComponent(this.getName().getFormattedText());
    }

    return new TranslationTextComponent(this.getName().getFormattedText());
  }

  /* Supporting methods */
  @Override
  public boolean isUsableByPlayer(@Nonnull PlayerEntity entityplayer) {
    // block changed/got broken?
    if (this.world.getTileEntity(this.pos) != this || this.world.getBlockState(this.pos).getBlock() == Blocks.AIR) {
      return false;
    }

    return
            entityplayer.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D)
                    <= 64D;
  }

  @Override
  public void openInventory(@Nonnull PlayerEntity player) {

  }

  @Override
  public void closeInventory(@Nonnull PlayerEntity player) {

  }

  /* NBT */
  @Override
  public void read(CompoundNBT tags) {
    super.read(tags);
    this.resizeInternal(tags.getInt("InventorySize"));

    this.readInventoryFromNBT(tags);

    if (tags.contains("CustomName", 8)) {
      this.inventoryTitle = ITextComponent.Serializer.fromJson(tags.getString("CustomName"));
    }
  }

  @Nonnull
  @Override
  public CompoundNBT write(CompoundNBT tags) {
    super.write(tags);

    tags.putInt("InventorySize", this.inventory.size());

    this.writeInventoryToNBT(tags);

    if (this.hasCustomName()) {
      tags.putString("CustomName", ITextComponent.Serializer.toJson(this.inventoryTitle));
    }
    return tags;
  }

  /** Writes the contents of the inventory to the tag */
  public void writeInventoryToNBT(CompoundNBT tag) {
    IInventory inventory = this;
    ListNBT nbttaglist = new ListNBT();

    for (int i = 0; i < inventory.getSizeInventory(); i++) {
      if (!inventory.getStackInSlot(i).isEmpty()) {
        CompoundNBT itemTag = new CompoundNBT();
        itemTag.putByte("Slot", (byte) i);
        inventory.getStackInSlot(i).write(itemTag);
        nbttaglist.add(itemTag);
      }
    }

    tag.put("Items", nbttaglist);
  }

  /** Reads a an inventory from the tag. Overwrites current content */
  public void readInventoryFromNBT(CompoundNBT tag) {
    ListNBT nbttaglist = tag.getList("Items", 10);

    int limit = this.getInventoryStackLimit();
    ItemStack stack;
    for (int i = 0; i < nbttaglist.size(); ++i) {
      CompoundNBT itemTag = nbttaglist.getCompound(i);
      int slot = itemTag.getByte("Slot") & 255;

      if (slot >= 0 && slot < this.inventory.size()) {
        stack = ItemStack.read(itemTag);
        if (!stack.isEmpty() && stack.getCount() > limit) {
          stack.setCount(limit);
        }
        this.inventory.set(slot, stack);
      }
    }
  }

  /* Default implementations of hardly used methods */
  @Nonnull
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
