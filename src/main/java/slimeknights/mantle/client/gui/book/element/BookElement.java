package slimeknights.mantle.client.gui.book.element;

import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.gui.book.GuiBook;

@SideOnly(Side.CLIENT)
public abstract class BookElement extends Gui {

  public GuiBook parent;

  public int x, y;

  public BookElement(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public abstract void draw(int mouseX, int mouseY, float partialTicks);

  public void mouseClicked(int mouseX, int mouseY, int mouseButton){

  }
}
