package slimeknights.mantle.client.book.data.element;

import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import slimeknights.mantle.client.book.repository.BookRepository;

public class DataLocation implements IDataElement {

  public String file;
  public transient Identifier location;

  @Override
  public void load(BookRepository source) {
    this.location = "$BLOCK_ATLAS".equals(this.file) ? PlayerScreenHandler.BLOCK_ATLAS_TEXTURE : source.getResourceLocation(this.file, true);
  }
}
