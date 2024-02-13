package slimeknights.mantle.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.network.packet.OpenNamedBookPacket.ClientOnly;

import javax.annotation.Nullable;

/** Packet sent by {@link slimeknights.mantle.command.ClearBookCacheCommand} to reset a book cache */
public record ClearBookCachePacket(@Nullable ResourceLocation book) implements IThreadsafePacket {
  public ClearBookCachePacket(FriendlyByteBuf buffer) {
    this(buffer.readBoolean() ? buffer.readResourceLocation() : null);
  }

  @Override
  public void encode(FriendlyByteBuf buf) {
    if (book != null) {
      buf.writeBoolean(true);
      buf.writeResourceLocation(book);
    } else {
      buf.writeBoolean(false);
    }
  }

  @Override
  public void handleThreadsafe(NetworkEvent.Context context) {
    if (book != null) {
      BookData bookData = BookLoader.getBook(book);
      if (bookData != null) {
        bookData.reset();
      } else {
        ClientOnly.errorStatus(book);
      }
    } else {
      BookLoader.resetAllBooks();
    }
  }
}
