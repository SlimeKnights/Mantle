package slimeknights.mantle.network.packet;

import lombok.AllArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.BookData;

@AllArgsConstructor
public class OpenNamedBookPacket implements IThreadsafePacket {
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
      bookData.openGui(new TextComponent("Book"), "", null, null);
    }
  }
}
