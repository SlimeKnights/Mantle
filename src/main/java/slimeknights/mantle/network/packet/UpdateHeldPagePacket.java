package slimeknights.mantle.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookHelper;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.mantle.network.MantleStreamCodecs;

/**
 * Packet to update the page in a book in the players hand
 */
public record UpdateHeldPagePacket(InteractionHand hand, String page) implements ISimplePacket {
  public static final Type<UpdateHeldPagePacket> TYPE = new Type<>(Mantle.getResource("update_held_page"));
  public static final StreamCodec<ByteBuf, UpdateHeldPagePacket> CODEC = StreamCodec.composite(
    MantleStreamCodecs.INTERACTION_HAND, UpdateHeldPagePacket::hand,
    ByteBufCodecs.stringUtf8(100), UpdateHeldPagePacket::page,
    UpdateHeldPagePacket::new);

  @Override
  public Type<UpdateHeldPagePacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    if (this.page != null) {
      ItemStack stack = context.player().getItemInHand(hand);
      if (!stack.isEmpty()) {
        BookHelper.writeSavedPageToBook(stack, this.page);
      }
    }
  }
}
