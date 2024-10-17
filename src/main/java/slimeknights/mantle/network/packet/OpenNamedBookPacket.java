package slimeknights.mantle.network.packet;

import lombok.AllArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.command.client.BookCommand;

@AllArgsConstructor
public class OpenNamedBookPacket implements IThreadsafePacket {
  private static final String BOOK_ERROR = "command.mantle.book_test.not_found";
  private final ResourceLocation book;

  public OpenNamedBookPacket(FriendlyByteBuf buffer) {
    this.book = buffer.readResourceLocation();
  }

  @Override
  public void encode(FriendlyByteBuf buf) {
    buf.writeResourceLocation(book);
  }

  @Override
  public void handleThreadsafe(NetworkEvent.Context context) {
    BookData bookData = BookLoader.getBook(book);
    if(bookData != null) {
      bookData.openGui(Component.literal("Book"), "", null, null);
    } else {
      ClientOnly.errorStatus(book);
    }
  }

  static class ClientOnly {
    static void errorStatus(ResourceLocation book) {
      BookCommand.bookNotFound(book);
    }
  }
}
