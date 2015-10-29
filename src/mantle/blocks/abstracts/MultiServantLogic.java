package mantle.blocks.abstracts;

import mantle.blocks.BlockUtils;
import mantle.blocks.iface.IMasterLogic;
import mantle.blocks.iface.IServantLogic;
import mantle.debug.DebugData;
import mantle.debug.IDebuggable;
import mantle.world.CoordTuple;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.GameRegistry;

public class MultiServantLogic extends TileEntity implements IServantLogic, IDebuggable
{
    boolean hasMaster;
    CoordTuple master;
    Block masterBlock;
    byte masterMeat; //Typo, it stays!

    public boolean canUpdate ()
    {
        return false;
    }

    public boolean getHasMaster ()
    {
        return hasMaster;
    }

    public boolean hasValidMaster ()
    {
        if (!hasMaster)
            return false;

        if (worldObj.getBlock(master.x, master.y, master.z) == masterBlock && worldObj.getBlockMetadata(master.x, master.y, master.z) == masterMeat)
            return true;

        else
        {
            hasMaster = false;
            master = null;
            return false;
        }
    }

    public CoordTuple getMasterPosition ()
    {
        return master;
    }

    public void overrideMaster (int x, int y, int z)
    {
        hasMaster = true;
        master = new CoordTuple(x, y, z);
        masterBlock = worldObj.getBlock(x, y, z);
        masterMeat = (byte) worldObj.getBlockMetadata(x, y, z);
    }

    public void removeMaster ()
    {
        hasMaster = false;
        master = null;
        masterBlock = null;
        masterMeat = 0;
    }

    @Override
    public boolean setPotentialMaster (IMasterLogic master, World w, int x, int y, int z)
    {
        return !hasMaster;
    }

    @Deprecated
    public boolean verifyMaster (IMasterLogic logic, int x, int y, int z)
    {
        if (master.equalCoords(x, y, z) && worldObj.getBlock(x, y, z) == masterBlock && worldObj.getBlockMetadata(x, y, z) == masterMeat)
            return true;
        else
            return false;
    }

    @Override
    public boolean verifyMaster (IMasterLogic logic, World world, int x, int y, int z)
    {
        if (hasMaster)
        {
            return hasValidMaster();
        }
        else
        {
            overrideMaster(x, y, z);
            return true;
        }
    }

    @Override
    public void invalidateMaster (IMasterLogic master, World w, int x, int y, int z)
    {
        hasMaster = false;
        master = null;
    }

    public void notifyMasterOfChange ()
    {
        if (hasValidMaster())
        {
            IMasterLogic logic = (IMasterLogic) worldObj.getTileEntity(master.x, master.y, master.z);
            logic.notifyChange(this, xCoord, yCoord, zCoord);
        }
    }

    public void readCustomNBT (NBTTagCompound tags)
    {
        hasMaster = tags.getBoolean("TiedToMaster");
        if (hasMaster)
        {
            int xCenter = tags.getInteger("xCenter");
            int yCenter = tags.getInteger("yCenter");
            int zCenter = tags.getInteger("zCenter");
            master = new CoordTuple(xCenter, yCenter, zCenter);
            masterBlock = BlockUtils.getBlockFromUniqueName(tags.getString("MasterBlockName"));
            masterMeat = tags.getByte("masterMeat");
        }
    }

    public void writeCustomNBT (NBTTagCompound tags)
    {
        tags.setBoolean("TiedToMaster", hasMaster);
        if (hasMaster)
        {
            tags.setInteger("xCenter", master.x);
            tags.setInteger("yCenter", master.y);
            tags.setInteger("zCenter", master.z);
            tags.setString("MasterBlockName", BlockUtils.getUniqueName(masterBlock));
            tags.setByte("masterMeat", masterMeat);
        }
    }

    @Override
    public void readFromNBT (NBTTagCompound tags)
    {
        super.readFromNBT(tags);
        readCustomNBT(tags);
    }

    @Override
    public void writeToNBT (NBTTagCompound tags)
    {
        super.writeToNBT(tags);
        writeCustomNBT(tags);
    }

    /* Packets */
    @Override
    public Packet getDescriptionPacket ()
    {
        NBTTagCompound tag = new NBTTagCompound();
        writeCustomNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
    }

    @Override
    public void onDataPacket (NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        readCustomNBT(packet.getNbtCompound());
        worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    /* IDebuggable */
    @Override
    public DebugData getDebugInfo (EntityPlayer player)
    {
        String[] strs = new String[2];
        strs[0] = "Location: x" + xCoord + ", y" + yCoord + ", z" + zCoord;
        if (hasMaster)
        {
            strs[1] = "masterBlock: " + masterBlock.toString() + ", masterMeat: " + masterMeat;
        }
        else
        {
            strs[1] = "No active master.";
        }
        return new DebugData(player, getClass(), strs);
    }

    public World getWorld ()
    {
        return this.getWorld();
    }

    @Deprecated
    public boolean setMaster (int x, int y, int z)
    {
        if (!hasMaster || worldObj.getBlock(master.x, master.y, master.z) != masterBlock || (worldObj.getBlockMetadata(master.x, master.y, master.z) != masterMeat))
        {
            overrideMaster(x, y, z);
            return true;
        }
        else
        {
            return false;
        }
    }

}
