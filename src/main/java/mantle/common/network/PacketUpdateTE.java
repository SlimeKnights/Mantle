package mantle.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;

public class PacketUpdateTE extends AbstractPacket
{
    private BlockPos pos;

    private NBTTagCompound data;

    public PacketUpdateTE()
    {

    }

    public PacketUpdateTE(BlockPos pos, NBTTagCompound data)
    {
        this.pos = pos;
        this.data = data;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        PacketBuffer pbuff = new PacketBuffer(buffer);
        pbuff.writeInt(this.pos.getX());
        pbuff.writeShort(this.pos.getY());
        pbuff.writeInt(this.pos.getZ());
        try
        {
            pbuff.writeNBTTagCompoundToBuffer(this.data);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
    {
        PacketBuffer pbuff = new PacketBuffer(buffer);
        this.pos = new BlockPos(pbuff.readInt(), pbuff.readShort(), pbuff.readInt());
        try
        {
            this.data = pbuff.readNBTTagCompoundFromBuffer();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void handleClientSide(EntityPlayer player)
    {
        TileEntity te = player.worldObj.getTileEntity(this.pos);

        if (te != null)
        {
            te.readFromNBT(this.data);
        }
    }

    @Override
    public void handleServerSide(EntityPlayer player)
    {
    }

}
