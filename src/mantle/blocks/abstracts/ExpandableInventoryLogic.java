package mantle.blocks.abstracts;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import com.google.common.collect.Lists;

public abstract class ExpandableInventoryLogic extends InventoryLogic implements IInventory
{

    public ExpandableInventoryLogic()
    {
        super(0);
    }

    protected ArrayList<ItemStack> inventory = Lists.newArrayList();

    protected String invName;

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        return slot < this.inventory.size() ? this.inventory.get(slot) : null;
    }

    @Override
    public boolean isStackInSlot(int slot)
    {
        return slot < this.inventory.size() && this.inventory.get(slot) != null;
    }

    @Override
    public int getSizeInventory()
    {
        return this.inventory.size();
    }

    public int getMaxSize()
    {
        return 64;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean canDropInventorySlot(int slot)
    {
        return true;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack itemstack)
    {
        if (slot < this.inventory.size())
        {
            this.inventory.set(slot, itemstack);
        }
        else if (slot == this.inventory.size())
        {
            this.inventory.add(itemstack);
        }
        else if (slot < this.getMaxSize())
        {
            this.inventory.ensureCapacity(slot);
            this.inventory.set(slot, itemstack);
        }
        else
        {
            return;
        }
        if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit())
        {
            itemstack.stackSize = this.getInventoryStackLimit();
        }
    }

    @Override
    public ItemStack decrStackSize(int slot, int quantity)
    {
        if (slot < this.inventory.size() && this.inventory.get(slot) != null)
        {
            if (this.inventory.get(slot).stackSize <= quantity)
            {
                ItemStack stack = this.inventory.get(slot);
                this.inventory.set(slot, null);
                return stack;
            }
            ItemStack split = this.inventory.get(slot).splitStack(quantity);
            if (this.inventory.get(slot).stackSize == 0)
            {
                this.inventory.set(slot, null);
            }
            return split;
        }
        else
        {
            return null;
        }
    }

    /* Supporting methods */
    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer)
    {
        if (this.worldObj.getTileEntity(this.pos) != this)
        {
            return false;
        }
        else
        {
            return entityplayer.getDistance((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64D;
        }

    }

    @Override
    public abstract Container getGuiContainer(InventoryPlayer inventoryplayer, World world, int x, int y, int z);

    /* NBT */
    @Override
    public void readFromNBT(NBTTagCompound tags)
    {
        super.readFromNBT(tags);
        this.invName = tags.getString("InvName");
        NBTTagList nbttaglist = tags.getTagList("Items", 9);
        this.inventory = new ArrayList<ItemStack>();
        this.inventory.ensureCapacity(nbttaglist.tagCount() > this.getMaxSize() ? this.getMaxSize() : nbttaglist.tagCount());
        for (int iter = 0; iter < nbttaglist.tagCount(); iter++)
        {
            NBTTagCompound tagList = nbttaglist.getCompoundTagAt(iter);
            byte slotID = tagList.getByte("Slot");
            if (slotID >= 0 && slotID < this.inventory.size())
            {
                this.inventory.set(slotID, ItemStack.loadItemStackFromNBT(tagList));
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tags)
    {
        super.writeToNBT(tags);
        if (this.invName != null)
        {
            tags.setString("InvName", this.invName);
        }
        NBTTagList nbttaglist = new NBTTagList();
        for (int iter = 0; iter < this.inventory.size(); iter++)
        {
            if (this.inventory.get(iter) != null)
            {
                NBTTagCompound tagList = new NBTTagCompound();
                tagList.setByte("Slot", (byte) iter);
                this.inventory.get(iter).writeToNBT(tagList);
                nbttaglist.appendTag(tagList);
            }
        }

        tags.setTag("Items", nbttaglist);
    }

    /* Default implementations of hardly used methods */
    @Override
    public ItemStack getStackInSlotOnClosing(int slot)
    {
        return null;
    }

    @Override
    public void openChest()
    {
    }

    @Override
    public void closeChest()
    {
    }

    @Override
    protected abstract String getDefaultName();

    @Override
    public void setInvName(String name)
    {
        this.invName = name;
    }

    @Override
    public String getInvName()
    {
        return this.isInvNameLocalized() ? this.invName : this.getDefaultName();
    }

    @Override
    public boolean isInvNameLocalized()
    {
        return this.invName != null && this.invName.length() > 0;
    }

    public void cleanInventory()
    {
        Iterator<ItemStack> i1 = this.inventory.iterator();
        while (i1.hasNext())
        {
            if (i1.next() == null)
            {
                i1.remove();
            }
        }
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack itemstack)
    {
        if (slot < this.getSizeInventory())
        {
            if (this.inventory.get(slot) == null || itemstack.stackSize + this.inventory.get(slot).stackSize <= this.getInventoryStackLimit())
            {
                return true;
            }
        }
        else
        {
            return slot < this.getMaxSize();
        }
        return false;
    }

}
