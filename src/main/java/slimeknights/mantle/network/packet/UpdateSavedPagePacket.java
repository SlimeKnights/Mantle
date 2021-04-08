package slimeknights.mantle.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import slimeknights.mantle.client.book.BookHelper;

public class UpdateSavedPagePacket implements IThreadsafePacket {

  private String pageName;

  public UpdateSavedPagePacket(String pageName) {
    this.pageName = pageName;
  }

  public UpdateSavedPagePacket(PacketByteBuf buffer) {
    this.pageName = buffer.readString(32767);
  }

  @Override
  public void encode(PacketByteBuf buf) {
    buf.writeString(this.pageName);
  }

  @Override
  public void handleThreadsafe(Context context) {
    PlayerEntity player = context.getSender();
    if (player != null && this.pageName != null) {
      ItemStack is = player.getStackInHand(Hand.MAIN_HAND);
      if (!is.isEmpty()) {
        BookHelper.writeSavedPage(is, this.pageName);
      }
    }
  }
}
