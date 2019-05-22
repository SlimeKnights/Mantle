package slimeknights.mantle.network.book;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;

import net.minecraftforge.fml.network.NetworkEvent;
import slimeknights.mantle.client.book.BookHelper;

import java.util.function.Supplier;

public class PacketUpdateSavedPage {

  private String pageName;

  public PacketUpdateSavedPage() {

  }

  public PacketUpdateSavedPage(String pageName) {
    this.pageName = pageName;
  }

  public static void encode(PacketUpdateSavedPage msg, PacketBuffer buf) {
    buf.writeString(msg.pageName);
  }

  public static PacketUpdateSavedPage decode(PacketBuffer buf) {
    return new PacketUpdateSavedPage(buf.readString(32767));
  }

  public static class Handler {
    public static void handle(final PacketUpdateSavedPage pkt, final Supplier<NetworkEvent.Context> ctx) {
      ctx.get().enqueueWork(() -> {
        if(ctx.get().getSender() != null && pkt.pageName != null) {
          EntityPlayer player = ctx.get().getSender();

          ItemStack is = player.getHeldItem(EnumHand.MAIN_HAND);

          if(!is.isEmpty()) {
            BookHelper.writeSavedPage(is, pkt.pageName);
          }
        }
      });

      ctx.get().setPacketHandled(true);
    }
  }
}
