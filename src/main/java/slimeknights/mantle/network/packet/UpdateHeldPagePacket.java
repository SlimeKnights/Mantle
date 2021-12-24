package slimeknights.mantle.network.packet;

import lombok.RequiredArgsConstructor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import slimeknights.mantle.client.book.BookHelper;

/**
 * Packet to update the page in a book in the players hand
 */
@RequiredArgsConstructor
public class UpdateHeldPagePacket implements IThreadsafePacket {
  private final Hand hand;
  private final String page;
  public UpdateHeldPagePacket(PacketBuffer buffer) {
    this.hand = buffer.readEnum(Hand.class);
    this.page = buffer.readUtf(100);
  }

  @Override
  public void encode(PacketBuffer buf) {
    buf.writeEnum(hand);
    buf.writeUtf(this.page);
  }

  @Override
  public void handleThreadsafe(Context context) {
    PlayerEntity player = context.getSender();
    if (player != null && this.page != null) {
      ItemStack stack = player.getItemInHand(hand);
      if (!stack.isEmpty()) {
        BookHelper.writeSavedPageToBook(stack, this.page);
      }
    }
  }
}
