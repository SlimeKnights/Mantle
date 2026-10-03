package slimeknights.mantle.client.screen.element;

import net.minecraft.client.gui.GuiGraphics;

/** Represents a repositionable GUI element. */
@SuppressWarnings("unused")  // API
public interface ScreenElement {
  /** Gets the width of the element */
  int width();

  /** Gets the height of the element */
  int height();
  /**
   * Draws the element at the given x/y coordinates
   *
   * @param xPos X-Coordinate on the screen
   * @param yPos Y-Coordinate on the screen
   */
  void draw(GuiGraphics graphics, int xPos, int yPos, int blitOffset);

  /**
   * Draws the element at the given x/y coordinates
   *
   * @param xPos X-Coordinate on the screen
   * @param yPos Y-Coordinate on the screen
   */
  default void draw(GuiGraphics graphics, int xPos, int yPos) {
    this.draw(graphics, xPos, yPos, 0);
  }


  /* Scaled */

  /**
   * Draws the element at the given location, overriding the default locations.
   * Used for scaled elements.
   */
  void drawInternal(GuiGraphics graphics, int xPos, int yPos, int blitOffset, int uOffset, int vOffset, int width, int height);

  /**
   * Draws the sprite scaled from left to right with tiling.
   * @param graphics     Graphics instance
   * @param xPos         X start position
   * @param yPos         Y start position
   * @param blitOffset   Drawing blit offset
   * @param width        Desired width. If it exceeds {@link #width()}, will tile.
   */
  default void drawScaledRight(GuiGraphics graphics, int xPos, int yPos, int blitOffset, int width) {
    int elementWidth = this.width();
    for (int i = 0; i < width / elementWidth; i++) {
      draw(graphics, xPos + i * elementWidth, yPos);
    }
    // remainder that doesn't fit total width
    int remainder = width % elementWidth;
    if (remainder > 0) {
      drawInternal(graphics, xPos + width - remainder, yPos, blitOffset, 0, 0, remainder, this.height());
    }
  }

  /**
   * Draws the sprite scaled from top to bottom with tiling.
   * @param graphics     Graphics instance
   * @param xPos         X start position
   * @param yPos         Y start position
   * @param blitOffset   Drawing blit offset
   * @param height       Desired height. If it exceeds {@link #height()}, will tile.
   */
  default void drawScaledDown(GuiGraphics graphics, int xPos, int yPos, int blitOffset, int height) {
    int elementHeight = this.height();
    for (int i = 0; i < height / elementHeight; i++) {
      this.draw(graphics, xPos, yPos + i * elementHeight);
    }
    // remainder that doesn't fit total width
    int remainder = height % elementHeight;
    if (remainder > 0) {
      drawInternal(graphics, xPos, yPos + height - remainder, blitOffset, 0, 0, this.width(), remainder);
    }
  }

  /**
   * Draws the sprite scaled from right to left without tiling.
   * @param graphics     Graphics instance
   * @param xPos         X position of top of image
   * @param yPos         Y position of top of image
   * @param width        Width to draw. Cannot exceed {@link #width()}
   */
  default void drawScaledLeft(GuiGraphics graphics, int xPos, int yPos, int blitOffset, int width) {
    // remainder that doesn't fit total height
    if (width > 0) {
      int offset = this.width() - width;
      drawInternal(graphics, xPos + offset, yPos, blitOffset, offset, 0, width, this.height());
    }
  }

  /**
   * Draws the sprite scaled from bottom to top without tiling.
   * @param graphics     Graphics instance
   * @param xPos         X position of top of image
   * @param yPos         Y position of top of image
   * @param height       Height to draw. Cannot exceed {@link #height()}
   */
  default void drawScaledUp(GuiGraphics graphics, int xPos, int yPos, int blitOffset, int height) {
    // remainder that doesn't fit total height
    if (height > 0) {
      int offset = this.height() - height;
      drawInternal(graphics, xPos, yPos + offset, blitOffset, 0, offset, this.width(), height);
    }
  }

  /**
   * Tiles the sprite to the given size the sprite scaled from left to right with tiling.
   * @param graphics     Graphics instance
   * @param xPos         X start position
   * @param yPos         Y start position
   * @param blitOffset   Drawing blit offset
   * @param width        Desired width. If it exceeds {@link #width()}, will tile.
   * @param height       Desired height. If it exceeds {@link #height()}, will tile.
   */
  default void drawTiled(GuiGraphics graphics, int xPos, int yPos, int blitOffset, int width, int height) {
    // we draw full height row-wise
    int elementHeight = this.height();
    int elementWidth = this.width();
    int full = height / elementHeight;
    for (int i = 0; i < full; i++) {
      drawScaledRight(graphics, xPos, yPos + i * elementHeight, blitOffset, width);
    }
    yPos += full * elementHeight;

    // and the remainder is drawn manually
    int remainderH = height % elementHeight;
    // the same as drawScaledLeft but with the remaining height
    for (int i = 0; i < width / elementWidth; i++) {
      drawScaledDown(graphics, xPos + i * elementWidth, yPos, blitOffset, remainderH);
    }
    // remainder that doesn't fit total width
    int remainderW = width % elementWidth;
    if (remainderW > 0) {
      drawInternal(graphics, xPos + width - remainderW, yPos, blitOffset, 0, 0, remainderW, remainderH);
    }
  }
}
