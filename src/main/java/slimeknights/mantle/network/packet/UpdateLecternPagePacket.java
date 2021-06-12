package slimeknights.mantle.network.packet;

import lombok.AllArgsConstructor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.LecternTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import slimeknights.mantle.client.book.BookHelper;
import slimeknights.mantle.util.TileEntityHelper;

/**
 * Packet to update the book page in a lectern
 */
@AllArgsConstructor
public class UpdateLecternPagePacket implements IThreadsafePacket {
  private final BlockPos pos;
  private final String page;
  public UpdateLecternPagePacket(PacketBuffer buffer) {
    this.pos = buffer.readBlockPos();
    this.page = buffer.readString(100);
  }

  @Override
  public void encode(PacketBuffer buf) {
    buf.writeBlockPos(pos);
    buf.writeString(page);
  }

  @Override
  public void handleThreadsafe(Context context) {
    PlayerEntity player = context.getSender();
    if (player != null && this.page != null) {
      World world = player.getEntityWorld();
      TileEntityHelper.getTile(LecternTileEntity.class, world, this.pos).ifPresent(te -> {
        ItemStack stack = te.getBook();
        if (!stack.isEmpty()) {
          BookHelper.writeSavedPageToBook(stack, this.page);
        }
      });
    }
  }
}
