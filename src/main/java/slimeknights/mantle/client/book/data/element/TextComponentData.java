package slimeknights.mantle.client.book.data.element;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.network.chat.Component;
import slimeknights.mantle.client.book.HTMLUtils;
import slimeknights.mantle.client.book.IHTML;
import slimeknights.mantle.client.book.data.BookData;

import javax.annotation.Nullable;

@Accessors(fluent = true)
@Setter
public class TextComponentData implements IHTML {
  /** @deprecated use {@link #linebreak} */
  @Deprecated
  public static final TextComponentData LINEBREAK = new TextComponentData((Component) null).linebreak(true);

  @Nullable
  public Component text;

  /** Adds 2 linebreaks before the text */
  public boolean isParagraph = false;
  /** If true, adds a line break after the text */
  public boolean linebreak = false;
  public boolean dropShadow = false;
  public float scale = 1;
  public String action = "";
  @Nullable
  public Component[] tooltips = null;

  public TextComponentData(@Nullable Component text) {
    this.text = text;
  }

  public TextComponentData(String text) {
    this(Component.literal(text));
  }

  // TODO: completely ignores styles in this class, but not in the component
  @Override
  public String toHTML(BookData book) {
    return HTMLUtils.p(text);
  }
}
