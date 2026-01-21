package slimeknights.mantle.client.book.data.element;

import net.minecraft.network.chat.Component;
import slimeknights.mantle.client.book.HTMLUtils;
import slimeknights.mantle.client.book.IHTML;
import slimeknights.mantle.client.book.data.BookData;

public class TextComponentData implements IHTML {

  public static final TextComponentData LINEBREAK = new TextComponentData("\n");

  public Component text;

  public boolean isParagraph = false;
  public boolean dropShadow = false;
  public float scale = 1.F;
  public String action = "";
  public Component[] tooltips = null;

  public TextComponentData(String text) {
    this(Component.literal(text));
  }

  public TextComponentData(Component text) {
    this.text = text;
  }

  @Override
  public String toHTML(BookData book) {
    return HTMLUtils.p(text);
  }
}
