package slimeknights.mantle.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.item.book.ILecternBookItem;
import slimeknights.mantle.network.ISimplePacket;

/**
 * Packet to open a book on a lectern
 */
public record OpenLecternBookPacket(BlockPos pos, ItemStack book) implements ISimplePacket {
  public static final Type<OpenLecternBookPacket> TYPE = new Type<>(Mantle.getResource("open_lectern_book"));
  public static final StreamCodec<RegistryFriendlyByteBuf, OpenLecternBookPacket> CODEC = StreamCodec.composite(
    BlockPos.STREAM_CODEC, OpenLecternBookPacket::pos,
    ItemStack.STREAM_CODEC, OpenLecternBookPacket::book,
    OpenLecternBookPacket::new);

  @Override
  public Type<OpenLecternBookPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    if (book.getItem() instanceof ILecternBookItem lecternBook) {
      lecternBook.openLecternScreenClient(pos, book);
    }
  }
}
