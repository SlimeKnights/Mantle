package slimeknights.mantle.client.gui.book;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static slimeknights.mantle.client.gui.book.Textures.TEX_BOOK;

@SideOnly(Side.CLIENT)
public class GuiArrow extends GuiButton {

  private static final int X = 412;

  public static final int WIDTH = 18;
  public static final int HEIGHT = 10;

  // Appearance
  public ArrowType arrowType;
  public int color;
  public int hoverColor;

  public GuiArrow(int buttonId, int x, int y, ArrowType arrowType, int color, int hoverColor) {
    super(buttonId, x, y, arrowType.w, arrowType.h, "");

    this.arrowType = arrowType;
    this.color = color;
    this.hoverColor = hoverColor;
  }

  @Override
  public void drawButton(Minecraft mc, int mouseX, int mouseY) {
    if(this.visible) {
      mc.getTextureManager().bindTexture(TEX_BOOK);

      this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

      int color = this.hovered ? this.hoverColor : this.color;

      float r = ((color >> 16) & 0xff) / 255.F;
      float g = ((color >> 8) & 0xff) / 255.F;
      float b = (color & 0xff) / 255.F;

      GlStateManager.color(r, g, b);
      Gui.drawScaledCustomSizeModalRect(xPosition, yPosition, arrowType.x, arrowType.y, width, height, width, height, 512, 512);
      this.mouseDragged(mc, mouseX, mouseY);
    }
  }

  public enum ArrowType {
    NEXT(412, 0),
    PREV(412, 10),
    RIGHT(412, 20),
    LEFT(412, 30),
    BACK_UP(412, 40, 18, 18);

    final int x, y, w, h;

    ArrowType(int x, int y) {
      this(x, y, WIDTH, HEIGHT);
    }

    ArrowType(int x, int y, int w, int h) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
    }
  }
}
