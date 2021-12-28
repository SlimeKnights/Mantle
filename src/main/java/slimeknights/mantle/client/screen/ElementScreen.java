package slimeknights.mantle.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;

/**
 * Represents a GUI element INSIDE the graphics file.
 * The coordinates all refer to the coordinates inside the graphics!
 */
public class ElementScreen {

  // this is totally completely ugly but it's a simple solution that doesn't clutter everything too much >_>
  public static int defaultTexW = 256;
  public static int defaultTexH = 256;

  public final int x;
  public final int y;
  public final int w;
  public final int h;

  public int texW;
  public int texH;

  public ElementScreen(int x, int y, int w, int h, int texW, int texH) {
    this(x, y, w, h);
    this.setTextureSize(texW, texH);

    defaultTexW = texW;
    defaultTexH = texH;
  }

  public ElementScreen(int x, int y, int w, int h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.setTextureSize(defaultTexW, defaultTexH);
  }

  public void setTextureSize(int w, int h) {
    this.texW = w;
    this.texH = h;
  }

  public ElementScreen shift(int xd, int yd) {
    return new ElementScreen(this.x + xd, this.y + yd, this.w, this.h, this.texW, this.texH);
  }

  /**
   * Draws the element at the given x/y coordinates
   *
   * @param xPos X-Coordinate on the screen
   * @param yPos Y-Coordinate on the screen
   */
  public int draw(PoseStack matrixStack, int xPos, int yPos) {
    Screen.blit(matrixStack, xPos, yPos, this.x, this.y, this.w, this.h, this.texW, this.texH);
    return this.w;
  }

  public static class Builder {

    public int w;
    public int h;

    public Builder(int w, int h) {
      this.w = w;
      this.h = h;
    }

    public ElementScreen get(int x, int y, int w, int h) {
      return new ElementScreen(x, y, w, h, this.w, this.h);
    }
  }
}
