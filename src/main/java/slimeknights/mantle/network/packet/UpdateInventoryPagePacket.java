package slimeknights.mantle.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookHelper;
import slimeknights.mantle.network.ISimplePacket;

/** Packet to update the page in a book in the players inventory */
public record UpdateInventoryPagePacket(int slot, String page) implements ISimplePacket {
  public static final Type<UpdateInventoryPagePacket> TYPE = new Type<>(Mantle.getResource("update_inventory_page"));
  public static final StreamCodec<ByteBuf, UpdateInventoryPagePacket> CODEC = StreamCodec.composite(
    ByteBufCodecs.VAR_INT, UpdateInventoryPagePacket::slot,
    ByteBufCodecs.stringUtf8(100), UpdateInventoryPagePacket::page,
    UpdateInventoryPagePacket::new);

  @Override
  public Type<UpdateInventoryPagePacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    if (this.page != null && slot >= 0) {
      ItemStack stack = context.player().getInventory().getItem(slot);
      if (!stack.isEmpty()) {
        BookHelper.writeSavedPageToBook(stack, this.page);
      }
    }
  }
}
