package slimeknights.mantle.client.screen;

import lombok.AllArgsConstructor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents a GUI element INSIDE the graphics file.
 * The coordinates all refer to the coordinates inside the graphics!
 */
@AllArgsConstructor
public class ElementScreen {
  // TODO: can this be final?
  public ResourceLocation texture;
  public final int x;
  public final int y;
  public final int w;
  public final int h;

  public final int texW;
  public final int texH;

  /** Creates a new element from this texture with the X, Y, width, and height */
  public ElementScreen move(int x, int y, int width, int height) {
    return new ElementScreen(this.texture, x, y, width, height, this.texW, this.texH);
  }

  /** Creates a new element by offsetting this element by the given amount */
  public ElementScreen shift(int xd, int yd) {
    return move(x + xd, y + yd, this.w, this.h);
  }

  /**
   * Draws the element at the given x/y coordinates
   *
   * @param xPos X-Coordinate on the screen
   * @param yPos Y-Coordinate on the screen
   */
  public void draw(GuiGraphics graphics, int xPos, int yPos) {
    graphics.blit(this.texture, xPos, yPos, this.x, this.y, this.w, this.h, this.texW, this.texH);
  }
}
