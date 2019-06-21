package slimeknights.mantle.network;

import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import io.netty.buffer.ByteBuf;

public abstract class AbstractPacket implements IMessage {

  public abstract IMessage handleClient(ClientPlayNetHandler netHandler);

  public abstract IMessage handleServer(ServerPlayNetHandler netHandler);

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
