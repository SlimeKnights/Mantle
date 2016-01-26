package slimeknights.mantle.client.gui.book.element;

import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author fuj1n
 */
@SideOnly(Side.CLIENT)
public abstract class BookElement extends Gui {

  public int x, y;

  public BookElement(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public abstract void draw(int mouseX, int mouseY, float partialTicks);
}
