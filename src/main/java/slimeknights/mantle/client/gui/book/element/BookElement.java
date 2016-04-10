package slimeknights.mantle.client.gui.book.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import slimeknights.mantle.client.gui.book.GuiBook;

@SideOnly(Side.CLIENT)
public abstract class BookElement extends Gui {

  public GuiBook parent;

  protected Minecraft mc = Minecraft.getMinecraft();
  protected FontRenderer fontRenderer = mc.fontRendererObj;
  protected TextureManager renderEngine = mc.renderEngine;

  public int x, y;

  public BookElement(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public abstract void draw(int mouseX, int mouseY, float partialTicks);

  public void drawOverlay(int mouseX, int mouseY, float partialTicks) {
  }

  public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

  }

  public void renderToolTip(FontRenderer fontRenderer, ItemStack stack, int x, int y) {
    List<String> list = stack.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);

    for(int i = 0; i < list.size(); ++i) {
      if(i == 0) {
        list.set(i, stack.getRarity().rarityColor + list.get(i));
      } else {
        list.set(i, TextFormatting.GRAY + list.get(i));
      }
    }

    FontRenderer font = stack.getItem().getFontRenderer(stack);
    drawHoveringText(list, x, y, (font == null ? fontRenderer : font));
  }

  public void drawHoveringText(List<String> textLines, int x, int y, FontRenderer font) {
    if(!textLines.isEmpty()) {
      GlStateManager.disableDepth();
      int i = 0;

      for(String s : textLines) {
        int j = font.getStringWidth(s);

        if(j > i) {
          i = j;
        }
      }

      int l1 = x + 12;
      int i2 = y - 12;
      int k = 8;

      if(textLines.size() > 1) {
        k += 2 + (textLines.size() - 1) * 10;
      }

      if(l1 + i > GuiBook.PAGE_WIDTH) {
        l1 -= 28 + i;
      }

      if(i2 + k + 6 > GuiBook.PAGE_HEIGHT) {
        i2 = GuiBook.PAGE_HEIGHT - k - 6;
      }

      int l = -267386864;
      this.drawGradientRect(l1 - 3, i2 - 4, l1 + i + 3, i2 - 3, l, l);
      this.drawGradientRect(l1 - 3, i2 + k + 3, l1 + i + 3, i2 + k + 4, l, l);
      this.drawGradientRect(l1 - 3, i2 - 3, l1 + i + 3, i2 + k + 3, l, l);
      this.drawGradientRect(l1 - 4, i2 - 3, l1 - 3, i2 + k + 3, l, l);
      this.drawGradientRect(l1 + i + 3, i2 - 3, l1 + i + 4, i2 + k + 3, l, l);
      int i1 = 1347420415;
      int j1 = (i1 & 16711422) >> 1 | i1 & -16777216;
      this.drawGradientRect(l1 - 3, i2 - 3 + 1, l1 - 3 + 1, i2 + k + 3 - 1, i1, j1);
      this.drawGradientRect(l1 + i + 2, i2 - 3 + 1, l1 + i + 3, i2 + k + 3 - 1, i1, j1);
      this.drawGradientRect(l1 - 3, i2 - 3, l1 + i + 3, i2 - 3 + 1, i1, i1);
      this.drawGradientRect(l1 - 3, i2 + k + 2, l1 + i + 3, i2 + k + 3, j1, j1);

      for(int k1 = 0; k1 < textLines.size(); ++k1) {
        String s1 = textLines.get(k1);
        font.drawStringWithShadow(s1, (float) l1, (float) i2, -1);

        if(k1 == 0) {
          i2 += 2;
        }

        i2 += 10;
      }

      GlStateManager.enableDepth();
    }
  }
}
