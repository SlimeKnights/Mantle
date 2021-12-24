package slimeknights.mantle.network.packet;

import lombok.AllArgsConstructor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
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
  public UpdateLecternPagePacket(FriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    this.page = buffer.readUtf(100);
  }

  @Override
  public void encode(FriendlyByteBuf buf) {
    buf.writeBlockPos(pos);
    buf.writeUtf(page);
  }

  @Override
  public void handleThreadsafe(Context context) {
    Player player = context.getSender();
    if (player != null && this.page != null) {
      Level world = player.getCommandSenderWorld();
      TileEntityHelper.getTile(LecternBlockEntity.class, world, this.pos).ifPresent(te -> {
        ItemStack stack = te.getBook();
        if (!stack.isEmpty()) {
          BookHelper.writeSavedPageToBook(stack, this.page);
        }
      });
    }
  }
}
