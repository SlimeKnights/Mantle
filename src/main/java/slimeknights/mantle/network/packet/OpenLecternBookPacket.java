package slimeknights.mantle.network.packet;

import lombok.AllArgsConstructor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import slimeknights.mantle.item.ILecternBookItem;

/**
 * Packet to open a book on a lectern
 */
@AllArgsConstructor
public class OpenLecternBookPacket implements IThreadsafePacket {
  private final BlockPos pos;
  private final ItemStack book;

  public OpenLecternBookPacket(PacketBuffer buffer) {
    this.pos = buffer.readBlockPos();
    this.book = buffer.readItemStack();
  }

  @Override
  public void encode(PacketBuffer buffer) {
    buffer.writeBlockPos(pos);
    buffer.writeItemStack(book);
  }

  @Override
  public void handleThreadsafe(Context context) {
    if (book.getItem() instanceof ILecternBookItem) {
      ((ILecternBookItem)book.getItem()).openLecternScreenClient(pos, book);
    }
  }
}
