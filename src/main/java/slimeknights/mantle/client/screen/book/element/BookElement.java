package slimeknights.mantle.client.screen.book.element;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import slimeknights.mantle.client.render.RenderingHelper;
import slimeknights.mantle.client.screen.book.BookScreen;

import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class BookElement extends DrawableHelper {

  public BookScreen parent;

  protected MinecraftClient mc = MinecraftClient.getInstance();
  protected TextureManager renderEngine = this.mc.textureManager;

  public int x, y;

  public BookElement(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public abstract void draw(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, TextRenderer fontRenderer);

  public void drawOverlay(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, TextRenderer fontRenderer) {
  }

  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {

  }

  public void mouseClickMove(double mouseX, double mouseY, int clickedMouseButton) {

  }

  public void mouseReleased(double mouseX, double mouseY, int clickedMouseButton) {

  }

  public void mouseDragged(int clickX, int clickY, int mx, int my, int lastX, int lastY, int button) {

  }

  public void renderToolTip(MatrixStack matrixStack, TextRenderer fontRenderer, ItemStack stack, int x, int y) {
    List<Text> list = stack.getTooltip(this.mc.player, this.mc.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL);

    TextRenderer font = stack.getItem().getFontRenderer(stack);
    if (font == null) {
      font = fontRenderer;
    }

    this.drawHoveringText(matrixStack, list, x, y, font);
    DiffuseLighting.disable();
  }

  public void drawHoveringText(MatrixStack matrixStack, List<Text> textLines, int x, int y, TextRenderer font) {
    RenderingHelper.drawHoveringText(matrixStack, textLines, x, y, this.parent.width, this.parent.height, -1, font);
    DiffuseLighting.disable();
  }
}
