package mantle.blocks.abstracts;

import mantle.debug.DebugData;
import mantle.debug.IDebuggable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Abstract logic for inventory Tile Entities.
 *
 * @author mDiyo
 */
public abstract class InventoryLogic extends TileEntity implements IInventory, IDebuggable
{
    protected ItemStack[] inventory;
    protected String invName;
    protected int stackSizeLimit;

    public InventoryLogic(int invSize)
    {
        this(invSize, 64);
    }

    public InventoryLogic(int invSize, int maxStackSize)
    {
        inventory = new ItemStack[invSize];
        stackSizeLimit = maxStackSize;
    }

    /* Inventory management */

    @Override
    public ItemStack getStackInSlot (int slot)
    {
        return inventory[slot];
    }

    public boolean isStackInSlot (int slot)
    {
        return inventory[slot] != null;
    }

    @Override
    public int getSizeInventory ()
    {
        return inventory.length;
    }

    @Override
    public int getInventoryStackLimit ()
    {
        return stackSizeLimit;
    }

    public boolean canDropInventorySlot (int slot)
    {
        return true;
    }

    @Override
    public void setInventorySlotContents (int slot, ItemStack itemstack)
    {
        inventory[slot] = itemstack;
        if (itemstack != null && itemstack.stackSize > getInventoryStackLimit())
        {
            itemstack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public ItemStack decrStackSize (int slot, int quantity)
    {
        if (inventory[slot] != null)
        {
            if (inventory[slot].stackSize <= quantity)
            {
                ItemStack stack = inventory[slot];
                inventory[slot] = null;
                return stack;
            }
            ItemStack split = inventory[slot].splitStack(quantity);
            if (inventory[slot].stackSize == 0)
            {
                inventory[slot] = null;
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
    public boolean isUseableByPlayer (EntityPlayer entityplayer)
    {
        //getBlockTileEntity
        if (getWorld().func_147438_o(field_145851_c, field_145848_d, field_145849_e) != this)
            return false;

        else
            return entityplayer.getDistance((double) field_145851_c + 0.5D, (double) field_145848_d + 0.5D, (double) field_145849_e + 0.5D) <= 64D;

    }

    public abstract Container getGuiContainer (InventoryPlayer inventoryplayer, World world, int x, int y, int z);

    /* NBT */
    @Override
    public void func_145841_b (NBTTagCompound tags)
    {
        super.func_145841_b(tags);
        readInventoryFromNBT(tags);
    }

    public void readInventoryFromNBT (NBTTagCompound tags)
    {
        super.func_145841_b(tags);
        this.invName = tags.getString("InvName");
        NBTTagList nbttaglist = tags.func_150295_c("Items", 0); //TODO WTF goes here <-
        inventory = new ItemStack[getSizeInventory()];
        for (int iter = 0; iter < nbttaglist.tagCount(); iter++)
        {
            NBTTagCompound tagList = (NBTTagCompound) nbttaglist.func_150305_b(iter); //TODO tagAt?
            byte slotID = tagList.getByte("Slot");
            if (slotID >= 0 && slotID < inventory.length)
            {
                inventory[slotID] = ItemStack.loadItemStackFromNBT(tagList);
            }
        }
    }

    @Override
    public void func_145839_a (NBTTagCompound tags)
    {
        super.func_145839_a(tags);
        writeInventoryToNBT(tags);
    }

    public void writeInventoryToNBT (NBTTagCompound tags)
    {
        if (invName != null)
            tags.setString("InvName", invName);
        NBTTagList nbttaglist = new NBTTagList();
        for (int iter = 0; iter < inventory.length; iter++)
        {
            if (inventory[iter] != null)
            {
                NBTTagCompound tagList = new NBTTagCompound();
                tagList.setByte("Slot", (byte) iter);
                inventory[iter].writeToNBT(tagList);
                nbttaglist.appendTag(tagList);
            }
        }

        tags.setTag("Items", nbttaglist);
    }

    /* Cause of the heisenbug. Do not uncomment! */
    /*public void superReadFromNBT (NBTTagCompound tags)
    {
        super.readFromNBT(tags);
    }
    
    public void superWriteToNBT (NBTTagCompound tags)
    {
        super.writeToNBT(tags);
    }*/

    /* Default implementations of hardly used methods */
    public ItemStack getStackInSlotOnClosing (int slot)
    {
        return null;
    }

    public void openChest ()
    {
    }

    public void closeChest ()
    {
    }

    protected abstract String getDefaultName ();

    public void setInvName (String name)
    {
        this.invName = name;
    }

    public String getInvName ()
    {
        return this.isInvNameLocalized() ? this.invName : getDefaultName();
    }

    public boolean isInvNameLocalized ()
    {
        return this.invName != null && this.invName.length() > 0;
    }

    @Override
    public boolean isItemValidForSlot (int slot, ItemStack itemstack)
    {
        if (slot < getSizeInventory())
        {
            if (inventory[slot] == null || itemstack.stackSize + inventory[slot].stackSize <= getInventoryStackLimit())
                return true;
        }
        return false;
    }

    public void placeBlock (EntityLivingBase entity, ItemStack stack)
    {

    }

    public void removeBlock ()
    {

    }
    public World getWorld()
    {
    return this.func_145831_w();
    }
    /* IDebuggable */
    @Override
    public DebugData getDebugInfo(EntityPlayer player) {
        String[] strs = new String[1];
        strs[0] = "invName: "+ invName + ", inventory.length: " + inventory.length + ", stackSizeLimit: " + stackSizeLimit;
        return new DebugData(player, getClass(), strs);
    }

}
