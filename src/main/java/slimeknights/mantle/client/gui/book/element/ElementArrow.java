package slimeknights.mantle.client.gui.book.element;

import net.minecraft.client.gui.FontRenderer;

import slimeknights.mantle.client.gui.book.GuiArrow;

public class ElementArrow extends ElementButton {

  protected final GuiArrow button;

  public ElementArrow(int id, IButtonClickHandler handler, int x, int y, GuiArrow.ArrowType arrowType, int arrowColor, int arrowColorHover) {
    super(id, x, y, arrowType.w, arrowType.h, handler);

    button = new GuiArrow(id, x, y, arrowType, arrowColor, arrowColorHover);
  }

  @Override
  public void draw(int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
    button.drawButton(mc, mouseX, mouseY, partialTicks);
  }

}
