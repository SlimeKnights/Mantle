package slimeknights.mantle.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.client.book.BookHelper;

/** Packet to update the page in a book in the players inventory */
public record UpdateInventoryPagePacket(int slot, String page) implements IThreadsafePacket {
  public UpdateInventoryPagePacket(FriendlyByteBuf buffer) {
    this(buffer.readVarInt(), buffer.readUtf(100));
  }

  @Override
  public void encode(FriendlyByteBuf buf) {
    buf.writeVarInt(slot);
    buf.writeUtf(page);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    Player player = context.player();
    if (player != null && this.page != null && slot >= 0) {
      ItemStack stack = player.getInventory().getItem(slot);
      if (!stack.isEmpty()) {
        BookHelper.writeSavedPageToBook(stack, this.page);
      }
    }
  }
}
