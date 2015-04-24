package mantle.blocks.abstracts;

import mantle.blocks.BlockUtils;
import mantle.blocks.iface.IMasterLogic;
import mantle.blocks.iface.IServantLogic;
import mantle.debug.DebugData;
import mantle.debug.IDebuggable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class MultiServantLogic extends TileEntity implements IServantLogic, IDebuggable
{
    boolean hasMaster;

    BlockPos master;

    Block masterBlock;

    IBlockState state;

    public boolean canUpdate()
    {
        return false;
    }

    public boolean getHasMaster()
    {
        return this.hasMaster;
    }

    public boolean hasValidMaster()
    {
        if (!this.hasMaster)
        {
            return false;
        }

        if (this.worldObj.getBlockState(this.master).getBlock() == this.masterBlock && this.worldObj.getBlockState(this.master) == this.state)
        {
            return true;
        }
        else
        {
            this.hasMaster = false;
            this.master = null;
            return false;
        }
    }

    @Override
    public BlockPos getMasterPosition()
    {
        return this.master;
    }

    public void overrideMaster(BlockPos pos)
    {
        this.hasMaster = true;
        this.master = pos;
        this.state = this.worldObj.getBlockState(this.master);
        this.masterBlock = this.state.getBlock();
    }

    public void removeMaster()
    {
        this.hasMaster = false;
        this.master = null;
        this.masterBlock = null;
        this.state = null;
    }

    @Override
    public boolean setPotentialMaster(IMasterLogic master, World w, BlockPos pos)
    {
        return !this.hasMaster;
    }

    @Deprecated
    public boolean verifyMaster(IMasterLogic logic, BlockPos pos)
    {
        return this.master.equals(pos) && this.worldObj.getBlockState(pos) == this.state && this.worldObj.getBlockState(pos).getBlock() == this.masterBlock;
    }

    @Override
    public boolean verifyMaster(IMasterLogic logic, World world, BlockPos pos)
    {
        if (this.hasMaster)
        {
            return this.hasValidMaster();
        }
        else
        {
            this.overrideMaster(pos);
            return true;
        }
    }

    @Override
    public void invalidateMaster(IMasterLogic master, World w, BlockPos pos)
    {
        this.hasMaster = false;
        master = null;
    }

    @Override
    public void notifyMasterOfChange()
    {
        if (this.hasValidMaster())
        {
            IMasterLogic logic = (IMasterLogic) this.worldObj.getTileEntity(this.pos);
            logic.notifyChange(this, this.pos);
        }
    }

    public void readCustomNBT(NBTTagCompound tags)
    {
        this.hasMaster = tags.getBoolean("TiedToMaster");
        if (this.hasMaster)
        {
            int xCenter = tags.getInteger("xCenter");
            int yCenter = tags.getInteger("yCenter");
            int zCenter = tags.getInteger("zCenter");
            this.master = new BlockPos(xCenter, yCenter, zCenter);
            this.masterBlock = BlockUtils.getBlockFromUniqueName(tags.getString("MasterBlockName"));
            // TODO: Make this a byte.
            this.state = Block.getStateById(tags.getInteger("masterState"));
        }
    }

    public void writeCustomNBT(NBTTagCompound tags)
    {
        tags.setBoolean("TiedToMaster", this.hasMaster);
        if (this.hasMaster)
        {
            tags.setInteger("xCenter", this.master.getX());
            tags.setInteger("yCenter", this.master.getY());
            tags.setInteger("zCenter", this.master.getZ());
            tags.setString("MasterBlockName", BlockUtils.getUniqueName(this.masterBlock));
            // TODO: Make this a byte.
            tags.setInteger("masterState", Block.getStateId(this.state));
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tags)
    {
        super.readFromNBT(tags);
        this.readCustomNBT(tags);
    }

    @Override
    public void writeToNBT(NBTTagCompound tags)
    {
        super.writeToNBT(tags);
        this.writeCustomNBT(tags);
    }

    /* Packets */
    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound tag = new NBTTagCompound();
        this.writeCustomNBT(tag);
        return new S35PacketUpdateTileEntity(this.pos, 1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        this.readCustomNBT(packet.getNbtCompound());
        this.worldObj.notifyLightSet(this.pos);
        this.worldObj.markBlockForUpdate(this.pos);
    }

    /* IDebuggable */
    @Override
    public DebugData getDebugInfo(EntityPlayer player)
    {
        String[] strs = new String[2];
        strs[0] = "Location: x" + this.pos.getX() + ", y" + this.pos.getY() + ", z" + this.pos.getZ();
        if (this.hasMaster)
        {
            strs[1] = "masterBlock: " + this.masterBlock.toString() + ", masterMeat: " + this.state.toString();
        }
        else
        {
            strs[1] = "No active master.";
        }
        return new DebugData(player, this.getClass(), strs);
    }

    @Override
    public World getWorld()
    {
        return this.worldObj;
    }

    @Deprecated
    public boolean setMaster(BlockPos pos)
    {
        if (!this.hasMaster || this.worldObj.getBlockState(this.master) != this.state || (this.worldObj.getBlockState(this.master).getBlock() != this.masterBlock))
        {
            this.overrideMaster(pos);
            return true;
        }
        else
        {
            return false;
        }
    }

}
