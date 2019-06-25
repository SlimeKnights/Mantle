package slimeknights.mantle.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class AbstractPacket {

  public abstract void encode(PacketBuffer buf);

  public abstract void handle(Supplier<NetworkEvent.Context> context);

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
