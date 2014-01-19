package mantle.blocks.abstracts;

import cpw.mods.fml.common.registry.GameRegistry;
import mantle.debug.DebugData;
import mantle.debug.IDebuggable;
import mantle.world.CoordTuple;
import mantle.blocks.iface.IMasterLogic;
import mantle.blocks.iface.IServantLogic;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

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

    public boolean hasValidMaster ( )
    {
        if (!hasMaster)
            return false;
        //Minecraft.getMinecraft().getMinecraft().thegetWorld()???
        if (getWorld().func_147439_a(master.x, master.y, master.z) == masterBlock && getWorld().getBlockMetadata(master.x, master.y, master.z) == masterMeat)
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
        masterBlock = getWorld().func_147439_a(x, y, z);
        masterMeat = (byte) getWorld().getBlockMetadata(x, y, z);
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

    @Override
    public boolean verifyMaster (IMasterLogic logic, World w, int x, int y, int z)
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
    public void invalidateMaster (IMasterLogic master,World w, int x, int y, int z)
    {
        hasMaster = false;
        master = null;
    }

    public void notifyMasterOfChange ()
    {
        if (hasValidMaster())
        {
            IMasterLogic logic = (IMasterLogic) getWorld().func_147438_o(master.x, master.y, master.z);
            logic.notifyChange(this, field_145851_c, field_145848_d, field_145849_e);
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
            masterBlock = GameRegistry.findBlock(tags.getString("MasterModName"), tags.getString("MasterBlockName"));
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
            tags.setString("MasterBlockName", masterBlock.func_149702_O());//<- unlocalized name?
            tags.setString("MasterModName", "MODNAME"); //TODO get mod name of block here!!
            tags.setByte("masterMeat", masterMeat);
        }
    }

    @Override
    public void func_145839_a (NBTTagCompound tags)
    {
        super.func_145839_a(tags);
        func_145841_b(tags);
        readCustomNBT(tags);
    }

    @Override
    public void func_145841_b (NBTTagCompound tags)
    {
        super.func_145841_b(tags);
        writeCustomNBT(tags);
    }

    /* Packets */
    //TODO getDescriptionPacket()??
    @Override
    public Packet func_145844_m ()
    {
        NBTTagCompound tag = new NBTTagCompound();
        writeCustomNBT(tag);
        //TODO xCoord, yCoord, zCoord
        return new S35PacketUpdateTileEntity(field_145851_c, field_145848_d, field_145849_e, 1, tag);
    }

    @Override
    public void onDataPacket (NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        readCustomNBT(packet.func_148857_g());
        //TODO markblockforRenderUpdate() ???
        getWorld().func_147471_g(field_145851_c, field_145848_d, field_145849_e);
    }

    /* IDebuggable */
    @Override
    public DebugData getDebugInfo (EntityPlayer player)
    {
        String[] strs = new String[2];
        strs[0] = "Location: x" + field_145851_c + ", y" + field_145848_d + ", z" + field_145849_e;
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
    public World getWorld()
        {
        return this.func_145831_w();
        }

}
