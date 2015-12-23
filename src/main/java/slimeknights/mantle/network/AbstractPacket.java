package slimeknights.mantle.network;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import io.netty.buffer.ByteBuf;

public abstract class AbstractPacket implements IMessage {

  public abstract IMessage handleClient(NetHandlerPlayClient netHandler);

  public abstract IMessage handleServer(NetHandlerPlayServer netHandler);

  protected void writePos(BlockPos pos, ByteBuf buf) {
    buf.writeInt(pos.getX());
    buf.writeInt(pos.getY());
    buf.writeInt(pos.getZ());
  }

  protected BlockPos readPos(ByteBuf buf) {
    int x = buf.readInt();
    int y = buf.readInt();
    int z = buf.readInt();
    return new BlockPos(x, y, z);
  }
}
