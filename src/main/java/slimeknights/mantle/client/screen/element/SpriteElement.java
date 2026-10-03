package slimeknights.mantle.client.screen.element;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** Repositionable element represented by a screen sprite. */
public record SpriteElement(ResourceLocation sprite, int width, int height) implements ScreenElement {
  @Override
  public void draw(GuiGraphics graphics, int xPos, int yPos, int blitOffset) {
    graphics.blitSprite(this.sprite, xPos, yPos, blitOffset, this.width, this.height);
  }

  @Override
  public void drawInternal(GuiGraphics graphics, int xPos, int yPos, int blitOffset, int uOffset, int vOffset, int width, int height) {
    graphics.blitSprite(this.sprite, this.width, this.height, uOffset, vOffset, xPos, yPos, blitOffset, width, height);
  }
}
