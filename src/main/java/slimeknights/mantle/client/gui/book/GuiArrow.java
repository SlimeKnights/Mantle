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
  public int arrowType;
  public int color;
  public int hoverColor;

  public GuiArrow(int buttonId, int x, int y, int arrowType, int color, int hoverColor) {
    super(buttonId, x, y, WIDTH, HEIGHT, "");

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
      Gui.drawScaledCustomSizeModalRect(xPosition, yPosition, X, HEIGHT * arrowType, WIDTH, HEIGHT, width, height, 512, 512);
      this.mouseDragged(mc, mouseX, mouseY);
    }
  }
}
