package slimeknights.mantle.client.gui.book.element;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class SizedBookElement extends BookElement {

  int width, height;

  public SizedBookElement(int x, int y, int width, int height) {
    super(x, y);

    this.width = width;
    this.height = height;
  }
}
