package slimeknights.mantle.client.gui.book;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author fuj1n
 */
@SideOnly(Side.CLIENT)
public class GuiArrow extends GuiButton {

  private static final ResourceLocation TEX_BOOK = new ResourceLocation("mantle:textures/gui/book.png");
  private static final int RIGHT_Y = 0;
  private static final int LEFT_Y = 14;
  private static final int X = 412;
  private static final int HOVER_X = 436;
  private static final int WIDTH = 18;
  private static final int HEIGHT = 10;

  private int direction;

  public GuiArrow(int buttonId, int x, int y, int direction) {
    super(buttonId, x, y, WIDTH, HEIGHT, "");

    this.direction = direction;
  }

  @Override
  public void drawButton(Minecraft mc, int mouseX, int mouseY) {
    if (this.visible) {
      mc.getTextureManager().bindTexture(TEX_BOOK);

      this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
      GlStateManager.color(1.0F, 1.0F, 1.0F, hovered ? 1.0F : 0.3F);

      Gui.drawModalRectWithCustomSizedTexture(xPosition, yPosition, (this.hovered ? HOVER_X : X), (direction == 0 ? RIGHT_Y : LEFT_Y), width, height, 512, 512);
      this.mouseDragged(mc, mouseX, mouseY);
    }
  }
}
