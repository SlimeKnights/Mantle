package slimeknights.mantle.client.book.data.element;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class TextComponentData {

  public static final TextComponentData LINEBREAK = new TextComponentData("\n");

  public Component text;

  public boolean isParagraph = false;
  public boolean dropShadow = false;
  public float scale = 1.F;
  public String action = "";
  public Component[] tooltips = null;

  public TextComponentData(String text) {
    this(new TextComponent(text));
  }

  public TextComponentData(Component text) {
    this.text = text;
  }


}
