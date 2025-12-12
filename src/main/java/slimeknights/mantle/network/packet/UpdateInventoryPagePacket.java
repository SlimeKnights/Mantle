package slimeknights.mantle.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookHelper;

/** Packet to update the page in a book in the players inventory */
public record UpdateInventoryPagePacket(int slot, String page) implements IThreadsafePacket {
  public static final CustomPacketPayload.Type<UpdateInventoryPagePacket> TYPE = new CustomPacketPayload.Type<>(Mantle.getResource("update_inventory_page"));
  public static final StreamCodec<RegistryFriendlyByteBuf, UpdateInventoryPagePacket> STREAM_CODEC = StreamCodec.composite(
    ByteBufCodecs.VAR_INT, UpdateInventoryPagePacket::slot,
    ByteBufCodecs.stringUtf8(100), UpdateInventoryPagePacket::page,
    UpdateInventoryPagePacket::new
  );

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
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
