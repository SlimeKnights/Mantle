package slimeknights.mantle.client.book.data.element;

import com.google.gson.JsonObject;
import slimeknights.mantle.client.book.repository.BookRepository;

import java.util.Map;

public class BlockData implements IDataElement {

  public int[] pos;
  public int[] endPos;
  public String block;
  public JsonObject nbt;
  public Map<String, String> state;

  @Override
  public void load(BookRepository source) {
    if (this.endPos == null) {
      this.endPos = this.pos;
    }
  }
}
