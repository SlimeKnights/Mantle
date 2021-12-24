package slimeknights.mantle.client.screen.book.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.GuiUtils;
import slimeknights.mantle.client.screen.book.BookScreen;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public abstract class BookElement extends AbstractGui {

  public BookScreen parent;

  protected Minecraft mc = Minecraft.getInstance();
  protected TextureManager renderEngine = this.mc.textureManager;

  public int x, y;

  public BookElement(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public abstract void draw(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer);

  public void drawOverlay(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
  }

  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {

  }

  /**
   *
   * @param mouseX mouse x position
   * @param mouseY mouse y position
   * @param clickedMouseButton the clicked button
   * @deprecated Goes unused and should be removed in 1.17 or book reworking
   */
  @Deprecated
  public void mouseClickMove(double mouseX, double mouseY, int clickedMouseButton) {

  }

  public void mouseReleased(double mouseX, double mouseY, int clickedMouseButton) {

  }

  public void mouseDragged(double clickX, double clickY, double mx, double my, double lastX, double lastY, int button) {

  }

  public void renderToolTip(MatrixStack matrixStack, FontRenderer fontRenderer, ItemStack stack, int x, int y) {
    List<ITextComponent> list = stack.getTooltipLines(this.mc.player, this.mc.options.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);

    FontRenderer font = stack.getItem().getFontRenderer(stack);
    if (font == null) {
      font = fontRenderer;
    }

    this.drawHoveringText(matrixStack, list, x, y, font);
  }

  public void drawHoveringText(MatrixStack matrixStack, List<ITextComponent> textLines, int x, int y, FontRenderer font) {
    //    GuiUtils.drawHoveringText(matrixStack, textLines, x, y, this.parent.width, this.parent.height, -1, font);
    GuiUtils.drawHoveringText(matrixStack, textLines, x, y, BookScreen.PAGE_WIDTH, BookScreen.PAGE_HEIGHT, BookScreen.PAGE_WIDTH, font);
  }
}
