package slimeknights.mantle.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import slimeknights.mantle.client.book.BookHelper;

public class UpdateSavedPagePacket implements IThreadsafePacket {

  private String pageName;

  public UpdateSavedPagePacket(String pageName) {
    this.pageName = pageName;
  }

  public UpdateSavedPagePacket(PacketBuffer buffer) {
    this.pageName = buffer.readString(32767);
  }

  @Override
  public void encode(PacketBuffer buf) {
    buf.writeString(this.pageName);
  }

  @Override
  public void handleThreadsafe(Context context) {
    PlayerEntity player = context.getSender();
    if (player != null && this.pageName != null) {
      ItemStack is = player.getHeldItem(Hand.MAIN_HAND);
      if (!is.isEmpty()) {
        BookHelper.writeSavedPageToBook(is, this.pageName);
      }
    }
  }
}
