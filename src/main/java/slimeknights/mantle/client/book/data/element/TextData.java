package slimeknights.mantle.client.book.data.element;

import net.minecraft.network.chat.Component;

public class TextData {

  public static final TextData LINEBREAK = new TextData("\n");

  public String text;
  public String color = "black";

  public int rgbColor = 0;
  public boolean useOldColor = true;
  public boolean bold = false;
  public boolean italic = false;
  public boolean underlined = false;
  public boolean strikethrough = false;
  public boolean obfuscated = false;
  public boolean paragraph = false;
  public boolean dropshadow = false;
  public float scale = 1.F;
  public String action = "";
  public Component[] tooltip = null;

  public TextData() {
  }

  public TextData(String text) {
    this.text = text;
  }
}
