package slimeknights.mantle.client.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ScalableElementScreen extends ElementScreen {

  public ScalableElementScreen(int x, int y, int w, int h, int texW, int texH) {
    super(x, y, w, h, texW, texH);
  }

  public ScalableElementScreen(int x, int y, int w, int h) {
    super(x, y, w, h);
  }

  public int drawScaledX(int xPos, int yPos, int width) {
    for (int i = 0; i < width / this.w; i++) {
      this.draw(xPos + i * this.w, yPos);
    }
    // remainder that doesn't fit total width
    int remainder = width % this.w;
    if (remainder > 0) {
      Screen.blit(xPos + width - remainder, yPos, this.x, this.y, remainder, this.h, this.texW, this.texH);
    }

    return width;
  }

  public int drawScaledY(int xPos, int yPos, int height) {
    for (int i = 0; i < height / this.h; i++) {
      this.draw(xPos, yPos + i * this.h);
    }
    // remainder that doesn't fit total width
    int remainder = height % this.h;
    if (remainder > 0) {
      Screen.blit(xPos, yPos + height - remainder, this.x, this.y, this.w, remainder, this.texW, this.texH);
    }

    return this.w;
  }

  public int drawScaled(int xPos, int yPos, int width, int height) {
    // we draw full height row-wise
    int full = height / this.h;
    for (int i = 0; i < full; i++) {
      this.drawScaledX(xPos, yPos + i * this.h, width);
    }

    yPos += full * this.h;

    // and the remainder is drawn manually
    int yRest = height % this.h;
    // the same as drawScaledX but with the remaining height
    for (int i = 0; i < width / this.w; i++) {
      this.drawScaledY(xPos + i * this.w, yPos, yRest);
    }
    // remainder that doesn't fit total width
    int remainder = width % this.w;
    if (remainder > 0) {
      Screen.blit(xPos + width - remainder, yPos, this.x, this.y, remainder, yRest, this.texW, this.texH);
    }

    return width;
  }

  @Override
  public ScalableElementScreen shift(int xd, int yd) {
    ScalableElementScreen element = new ScalableElementScreen(this.x + xd, this.y + yd, this.w, this.h);
    element.setTextureSize(this.texW, this.texH);
    return element;
  }
}
