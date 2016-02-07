package slimeknights.mantle.client.gui.book.element;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.stats.Achievement;
import net.minecraft.util.EnumChatFormatting;
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
  public void draw(int mouseX, int mouseY, float partialTicks) {
    boolean unlocked = section.isUnlocked(parent.statFile);
    boolean hover = mouseX > x && mouseY > y && mouseX < x + WIDTH && mouseY < y + HEIGHT;

    if (section.icon != null) {
      Minecraft.getMinecraft().renderEngine.bindTexture(section.icon.location);

      if (unlocked)
        GlStateManager.color(1F, 1F, 1F, hover ? 1F : 0.5F);
      else {
        float r = ((parent.book.appearance.lockedSectionColor >> 16) & 0xff) / 255.F;
        float g = ((parent.book.appearance.lockedSectionColor >> 8) & 0xff) / 255.F;
        float b = (parent.book.appearance.lockedSectionColor & 0xff) / 255.F;
        GlStateManager.color(r, g, b, 0.75F);
      }

      drawScaledCustomSizeModalRect(x + WIDTH / 2 - IMG_SIZE / 2, y + HEIGHT / 2 - IMG_SIZE / 2, section.icon.u, section.icon.v, section.icon.uw, section.icon.vh, IMG_SIZE, IMG_SIZE, section.icon.texWidth, section.icon.texHeight);
    }

    Minecraft.getMinecraft().fontRendererObj.drawString(section.title, x + WIDTH / 2 - Minecraft.getMinecraft().fontRendererObj.getStringWidth(section.title) / 2, y + HEIGHT - Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT, hover ? 0xFF000000 : 0x7F000000);
  }

  @Override
  public void drawOverlay(int mouseX, int mouseY, float partialTicks) {
    if (section != null && !section.isUnlocked(parent.statFile) && mouseX > x && mouseY > y && mouseX < x + WIDTH && mouseY < y + HEIGHT) {
      List<String> l = new ArrayList<>();
      l.add(EnumChatFormatting.RED + "Locked");
      l.add("Requirements:");

      for (String requirement : section.requirements) {
        Achievement achievement = SectionData.findAchievement(requirement);
        if (achievement != null)
          l.add((SectionData.requirementSatisfied(requirement, parent.statFile) ? EnumChatFormatting.GREEN : EnumChatFormatting.RED) + EnumChatFormatting.getTextWithoutFormattingCodes(achievement.getStatName().getFormattedText()));
      }

      drawHoveringText(l, mouseX, mouseY, Minecraft.getMinecraft().fontRendererObj);
    }
  }

  public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    if (mouseButton == 0 && section != null && section.isUnlocked(parent.statFile) && mouseX > x && mouseY > y && mouseX < x + WIDTH && mouseY < y + HEIGHT)
      parent.openPage(parent.book.getFirstPageNumber(section, parent.statFile));
  }
}
