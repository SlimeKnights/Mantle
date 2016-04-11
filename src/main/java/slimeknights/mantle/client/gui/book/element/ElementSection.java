package slimeknights.mantle.client.gui.book.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.stats.Achievement;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

import slimeknights.mantle.client.book.data.SectionData;

public class ElementSection extends BookElement {

  public static final int IMG_SIZE = 64;

  public static final int WIDTH = 85;
  public static final int HEIGHT = 85;

  private SectionData section;

  public ElementSection(int x, int y, SectionData section) {
    super(x, y);

    this.section = section;
  }

  @Override
  public void draw(int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
    boolean unlocked = section.isUnlocked(parent.statFile);
    boolean hover = mouseX > x && mouseY > y && mouseX < x + WIDTH && mouseY < y + HEIGHT;

    if(section.icon != null) {
      if(unlocked) {
        GlStateManager.color(1F, 1F, 1F, hover ? 1F : 0.5F);
      } else {
        float r = ((parent.book.appearance.lockedSectionColor >> 16) & 0xff) / 255.F;
        float g = ((parent.book.appearance.lockedSectionColor >> 8) & 0xff) / 255.F;
        float b = (parent.book.appearance.lockedSectionColor & 0xff) / 255.F;
        GlStateManager.color(r, g, b, 0.75F);
      }

      if(section.icon.item == null) {
        if(section.icon.location != null) {
          renderEngine.bindTexture(section.icon.location);

          drawScaledCustomSizeModalRect(x + WIDTH / 2 - IMG_SIZE / 2, y + HEIGHT / 2 - IMG_SIZE / 2, section.icon.u, section.icon.v, section.icon.uw, section.icon.vh, IMG_SIZE, IMG_SIZE, section.icon.texWidth, section.icon.texHeight);
        }
      } else {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + WIDTH / 2 - IMG_SIZE / 2, y + HEIGHT / 2 - IMG_SIZE / 2, 0);
        GlStateManager.scale(4F, 4F, 1F);
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(section.icon.item.getItems()[0], 0, 0);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
      }
    }

    fontRenderer.drawString(section.getTitle(), x + WIDTH / 2 - fontRenderer.getStringWidth(section
                                                                                                .getTitle()) / 2, y + HEIGHT - fontRenderer.FONT_HEIGHT, hover ? 0xFF000000 : 0x7F000000);
  }

  @Override
  public void drawOverlay(int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
    if(section != null && !section
        .isUnlocked(parent.statFile) && mouseX > x && mouseY > y && mouseX < x + WIDTH && mouseY < y + HEIGHT) {
      List<String> l = new ArrayList<>();
      l.add(TextFormatting.RED + "Locked");
      l.add("Requirements:");

      for(String requirement : section.requirements) {
        Achievement achievement = SectionData.findAchievement(requirement);
        if(achievement != null) {
          l.add((SectionData
                     .requirementSatisfied(requirement, parent.statFile) ? TextFormatting.GREEN : TextFormatting.RED) + TextFormatting
                    .getTextWithoutFormattingCodes(achievement.getStatName().getFormattedText()));
        }
      }

      drawHoveringText(l, mouseX, mouseY, Minecraft.getMinecraft().fontRendererObj);
    }
  }

  public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    if(mouseButton == 0 && section != null && section
        .isUnlocked(parent.statFile) && mouseX > x && mouseY > y && mouseX < x + WIDTH && mouseY < y + HEIGHT) {
      parent.openPage(parent.book.getFirstPageNumber(section, parent.statFile));
    }
  }
}
