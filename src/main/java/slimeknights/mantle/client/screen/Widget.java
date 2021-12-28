package slimeknights.mantle.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;

public abstract class Widget {

  public int xPos;
  public int yPos;
  public int height;
  public int width;

  public abstract void draw(PoseStack matrixStack);

  /**
   * Where the part will be drawn. Upper left corner.
   */
  public void setPosition(int x, int y) {
    this.xPos = x;
    this.yPos = y;
  }

  public void setSize(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public void handleMouseClicked(int mouseX, int mouseY, int mouseButton) {

  }

  public void handleMouseReleased() {

  }
}

