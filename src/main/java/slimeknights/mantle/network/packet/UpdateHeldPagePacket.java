package slimeknights.mantle.network.packet;

import lombok.RequiredArgsConstructor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import slimeknights.mantle.client.book.BookHelper;

/**
 * Packet to update the page in a book in the players hand
 */
@RequiredArgsConstructor
public class UpdateHeldPagePacket implements IThreadsafePacket {
  private final InteractionHand hand;
  private final String page;
  public UpdateHeldPagePacket(FriendlyByteBuf buffer) {
    this.hand = buffer.readEnum(InteractionHand.class);
    this.page = buffer.readUtf(100);
  }

  @Override
  public void encode(FriendlyByteBuf buf) {
    buf.writeEnum(hand);
    buf.writeUtf(this.page);
  }

  @Override
  public void handleThreadsafe(Context context) {
    Player player = context.getSender();
    if (player != null && this.page != null) {
      ItemStack stack = player.getItemInHand(hand);
      if (!stack.isEmpty()) {
        BookHelper.writeSavedPageToBook(stack, this.page);
      }
    }
  }
}
