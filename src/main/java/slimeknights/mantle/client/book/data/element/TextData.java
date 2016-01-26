package slimeknights.mantle.client.book.data.element;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author fuj1n
 */
@SideOnly(Side.CLIENT)
public class TextData {

  public String text;
  public String color = "black";
  public boolean bold = false;
  public boolean italic = false;
  public boolean underlined = false;
  public boolean strikethrough = false;
  public boolean obfuscated = false;
  public String onClick = "";
  public String[] tooltip = null;

  public TextData() {
  }

  public TextData(String text) {
    this.text = text;
  }
}
