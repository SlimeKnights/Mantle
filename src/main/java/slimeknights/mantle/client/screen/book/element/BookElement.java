package slimeknights.mantle.client.screen.book.element;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.extensions.common.IClientItemExtensions.FontContext;
import slimeknights.mantle.client.screen.book.BookScreen;

import java.util.List;
import java.util.Optional;

public abstract class BookElement extends GuiComponent {

  public BookScreen parent;

  protected Minecraft mc = Minecraft.getInstance();
  protected TextureManager renderEngine = this.mc.textureManager;

  public int x, y;

  public BookElement(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public abstract void draw(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks, Font fontRenderer);

  public void drawOverlay(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks, Font fontRenderer) {
  }

  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {

  }

  public void mouseReleased(double mouseX, double mouseY, int clickedMouseButton) {

  }

  public void mouseDragged(double clickX, double clickY, double mx, double my, double lastX, double lastY, int button) {

  }

  public void renderToolTip(PoseStack matrixStack, Font fontRenderer, ItemStack stack, int x, int y) {
    List<Component> list = stack.getTooltipLines(this.mc.player, this.mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);

    Font font = IClientItemExtensions.of(stack).getFont(stack, FontContext.TOOLTIP);
    if (font == null) {
      font = fontRenderer;
    }

    this.drawTooltip(matrixStack, list, x, y, font);
  }

  public void drawTooltip(PoseStack matrixStack, List<Component> textLines, int x, int y, Font font) {
    // GuiUtils.drawHoveringText(matrixStack, textLines, x, y, this.parent.width, this.parent.height, -1, font);
    // GuiUtils.drawHoveringText(matrixStack, textLines, x, y, BookScreen.PAGE_WIDTH, BookScreen.PAGE_HEIGHT, BookScreen.PAGE_WIDTH, font);
    int oldWidth = parent.width;
    int oldHeight = parent.height;
    parent.width = BookScreen.PAGE_WIDTH;
    parent.height = BookScreen.PAGE_HEIGHT;
    parent.renderTooltip(matrixStack, textLines, Optional.empty(), x, y, font);
    parent.width = oldWidth;
    parent.height = oldHeight;
  }
}
