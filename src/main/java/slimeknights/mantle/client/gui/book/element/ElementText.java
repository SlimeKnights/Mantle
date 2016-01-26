package slimeknights.mantle.client.gui.book.element;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.gui.book.TextDataRenderer;

@SideOnly(Side.CLIENT)
public class ElementText extends SizedBookElement {

  public TextData[] text;

  public ElementText(int x, int y, int width, int height, String text) {
    this(x, y, width, height, new TextData[]{new TextData(text)});
  }

  public ElementText(int x, int y, int width, int height, TextData[] text) {
    super(x, y, width, height);

    this.text = text;
  }

  @Override
  public void draw(int mouseX, int mouseY, float partialTicks) {
    TextDataRenderer.drawText(x, y, width, height, text, mouseX, mouseY);
  }
}
