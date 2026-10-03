package slimeknights.mantle.client.screen.element;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** Repositionable element represented by a texture with position internally. */
public record TextureElement(ResourceLocation texture, int u, int v, int width, int height, int texWidth, int texHeight) implements ScreenElement {
  public TextureElement(ResourceLocation texture, int u, int v, int width, int height) {
    this(texture, u, v, width, height, width, height);
  }

  /** Creates a new element from this texture with the X, Y, width, and height */
  public TextureElement move(int u, int v, int width, int height) {
    return new TextureElement(this.texture, u, v, width, height, this.texWidth, this.texHeight);
  }

  /** Creates a new element by offsetting this element by the given amount */
  public TextureElement shift(int uOffset, int vOffset) {
    return move(u + uOffset, v + vOffset, this.width, this.height);
  }

  @Override
  public void draw(GuiGraphics graphics, int xPos, int yPos, int blitOffset) {
    graphics.blit(texture, xPos, yPos, blitOffset, this.u, this.v, this.width, this.height, this.texWidth, this.texHeight);
  }

  @Override
  public void drawInternal(GuiGraphics graphics, int xPos, int yPos, int blitOffset, int uOffset, int vOffset, int width, int height) {
    graphics.blit(texture, xPos, yPos, blitOffset, this.u + uOffset, this.v + vOffset, width, height, this.texWidth, this.texHeight);
  }
}
