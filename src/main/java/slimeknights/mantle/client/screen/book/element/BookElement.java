package slimeknights.mantle.client.screen.book.element;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.config.GuiUtils;
import slimeknights.mantle.client.screen.book.BookScreen;

import java.util.List;
import java.util.stream.Collectors;

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

  public abstract void draw(int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer);

  public void drawOverlay(int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
  }

  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {

  }

  public void mouseClickMove(double mouseX, double mouseY, int clickedMouseButton) {

  }

  public void mouseReleased(double mouseX, double mouseY, int clickedMouseButton) {

  }

  public void mouseDragged(int clickX, int clickY, int mx, int my, int lastX, int lastY, int button) {

  }

  public void renderToolTip(FontRenderer fontRenderer, ItemStack stack, int x, int y) {
    if (stack != null) {
      List<ITextComponent> list = stack.getTooltip(this.mc.player, this.mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
      List<String> list1 = list.stream().map(ITextComponent::getFormattedText).collect(Collectors.toList());

      FontRenderer font = stack.getItem().getFontRenderer(stack);
      if (font == null) {
        font = fontRenderer;
      }
      GuiUtils.drawHoveringText(list1, x, y, BookScreen.PAGE_WIDTH, BookScreen.PAGE_HEIGHT, -1, font);
      RenderHelper.disableStandardItemLighting();
    }
  }

  @Deprecated
  public void drawHoveringText(List<String> textLines, int x, int y, FontRenderer font) {
    if (!textLines.isEmpty()) {
      GlStateManager.disableDepthTest();
      int i = 0;

      for (String s : textLines) {
        int j = font.getStringWidth(s);

        if (j > i) {
          i = j;
        }
      }

      int l1 = x + 12;
      int i2 = y - 12;
      int k = 8;

      if (textLines.size() > 1) {
        k += 2 + (textLines.size() - 1) * 10;
      }

      if (l1 + i > BookScreen.PAGE_WIDTH) {
        l1 -= 28 + i;
      }

      if (i2 + k + 6 > BookScreen.PAGE_HEIGHT) {
        i2 = BookScreen.PAGE_HEIGHT - k - 6;
      }

      int l = -267386864;
      this.fillGradient(l1 - 3, i2 - 4, l1 + i + 3, i2 - 3, l, l);
      this.fillGradient(l1 - 3, i2 + k + 3, l1 + i + 3, i2 + k + 4, l, l);
      this.fillGradient(l1 - 3, i2 - 3, l1 + i + 3, i2 + k + 3, l, l);
      this.fillGradient(l1 - 4, i2 - 3, l1 - 3, i2 + k + 3, l, l);
      this.fillGradient(l1 + i + 3, i2 - 3, l1 + i + 4, i2 + k + 3, l, l);
      int i1 = 1347420415;
      int j1 = (i1 & 16711422) >> 1 | i1 & -16777216;
      this.fillGradient(l1 - 3, i2 - 3 + 1, l1 - 3 + 1, i2 + k + 3 - 1, i1, j1);
      this.fillGradient(l1 + i + 2, i2 - 3 + 1, l1 + i + 3, i2 + k + 3 - 1, i1, j1);
      this.fillGradient(l1 - 3, i2 - 3, l1 + i + 3, i2 - 3 + 1, i1, i1);
      this.fillGradient(l1 - 3, i2 + k + 2, l1 + i + 3, i2 + k + 3, j1, j1);

      for (int k1 = 0; k1 < textLines.size(); ++k1) {
        String s1 = textLines.get(k1);
        font.drawStringWithShadow(s1, (float) l1, (float) i2, -1);

        if (k1 == 0) {
          i2 += 2;
        }

        i2 += 10;
      }

      GlStateManager.enableDepthTest();
    }
  }
}
