package slimeknights.mantle.client.gui.book.element;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.text.TextFormatting;
import slimeknights.mantle.client.book.data.SectionData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ElementSection extends SizedBookElement {

  public static final int IMG_SIZE = 32;

  public static final int WIDTH = 42;
  public static final int HEIGHT = 42;

  private SectionData section;

  public ElementSection(int x, int y, SectionData section) {
    super(x, y, WIDTH, HEIGHT);

    this.section = section;
  }

  @Override
  public void draw(int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
    boolean unlocked = section.isUnlocked(parent.advancementCache);
    boolean hover = isHovered(mouseX, mouseY);

    if(section.icon != null) {
      int iconX = x + WIDTH / 2 - IMG_SIZE / 2;
      int iconY = y + HEIGHT / 2 - IMG_SIZE / 2;
      if(hover) {
        drawRect(iconX, iconY, iconX + IMG_SIZE, iconY + IMG_SIZE, parent.book.appearance.hoverColor);
      }
      if(unlocked) {
        GlStateManager.color4f(1F, 1F, 1F, hover ? 1F : 0.5F);
      } else {
        float r = ((parent.book.appearance.lockedSectionColor >> 16) & 0xff) / 255.F;
        float g = ((parent.book.appearance.lockedSectionColor >> 8) & 0xff) / 255.F;
        float b = (parent.book.appearance.lockedSectionColor & 0xff) / 255.F;
        GlStateManager.color4f(r, g, b, 0.75F);
      }

      if(section.icon.item == null) {
        if(section.icon.location != null) {
          renderEngine.bindTexture(section.icon.location);

          drawScaledCustomSizeModalRect(iconX, iconY, section.icon.u, section.icon.v, section.icon.uw, section.icon.vh, IMG_SIZE, IMG_SIZE, section.icon.texWidth, section.icon.texHeight);
        }
      } else {
        GlStateManager.pushMatrix();
        GlStateManager.translatef(iconX, iconY, 0);
        GlStateManager.scalef(2F, 2F, 1F);
        RenderHelper.enableGUIStandardItemLighting();
        mc.getItemRenderer().renderItemAndEffectIntoGUI(section.icon.item.getItems().get(0), 0, 0);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
      }
    }

    if(section.parent.appearance.drawSectionListText) {
      int textW = fontRenderer.getStringWidth(section.getTitle());
      int textX = x + WIDTH / 2 - textW / 2;
      int textY = y + HEIGHT - fontRenderer.FONT_HEIGHT/2;
      fontRenderer.drawString(section.getTitle(),
                              textX,
                              textY,
                              hover ? 0xFF000000 : 0x7F000000);
    }
  }

  @Override
  public void drawOverlay(int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
    if(section != null && isHovered(mouseX, mouseY)) {
      List<String> text = new ArrayList<>();
      text.add(section.getTitle());
      if(!section.isUnlocked(parent.advancementCache)) {
        text.add(TextFormatting.RED + "Locked");
        text.add("Requirements:");

        text.addAll(section.requirements);
      }
      drawHoveringText(text, mouseX, mouseY, fontRenderer);
    }
  }

  @Override
  public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    if(mouseButton == 0 && section != null && section.isUnlocked(parent.advancementCache) && isHovered(mouseX, mouseY)) {
      parent.openPage(parent.book.getFirstPageNumber(section, parent.advancementCache));
    }
  }
}
