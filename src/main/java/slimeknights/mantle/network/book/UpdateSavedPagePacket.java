package slimeknights.mantle.network.book;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;
import slimeknights.mantle.client.book.BookHelper;
import slimeknights.mantle.network.AbstractPacket;

import java.util.function.Supplier;

public class UpdateSavedPagePacket extends AbstractPacket {

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
  public void handle(Supplier<NetworkEvent.Context> context) {
    context.get().enqueueWork(() -> {
      if (context.get().getSender() != null && this.pageName != null) {
        PlayerEntity player = context.get().getSender();
        if (player != null) {
          ItemStack is = player.getHeldItem(Hand.MAIN_HAND);
          if (!is.isEmpty()) {
            BookHelper.writeSavedPage(is, this.pageName);
          }
        }
      }
    });

    context.get().setPacketHandled(true);
  }
}
