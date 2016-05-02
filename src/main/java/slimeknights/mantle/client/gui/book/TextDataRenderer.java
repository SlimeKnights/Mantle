package slimeknights.mantle.client.gui.book;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.data.element.TextData;

@SideOnly(Side.CLIENT)
public class TextDataRenderer {

  public static String drawText(int x, int y, int boxWidth, int boxHeight, TextData[] data, int mouseX, int mouseY, FontRenderer fr) {
    String action = "";

    String[] drawLabel = null;

    int atX = x;
    int atY = y;

    float prevScale = 1.F;

    for(TextData item : data) {
      int box1X, box1Y, box1W = 9999, box1H = y + fr.FONT_HEIGHT;
      int box2X, box2Y = 9999, box2W, box2H;
      int box3X = 9999, box3Y = 9999, box3W, box3H;

      if(item.text == null || item.text.isEmpty()) {
        continue;
      }
      if(item.text.equals("\n")) {
        atX = x;
        atY += fr.FONT_HEIGHT;
        continue;
      }

      if(item.paragraph) {
        atX = x;
        atY += fr.FONT_HEIGHT * 2 * prevScale;
      }

      prevScale = item.scale;

      String modifiers = "";

      modifiers += TextFormatting.getValueByName(item.color);

      if(item.bold) {
        modifiers += TextFormatting.BOLD;
      }
      if(item.italic) {
        modifiers += TextFormatting.ITALIC;
      }
      if(item.underlined) {
        modifiers += TextFormatting.UNDERLINE;
      }
      if(item.strikethrough) {
        modifiers += TextFormatting.STRIKETHROUGH;
      }
      if(item.obfuscated) {
        modifiers += TextFormatting.OBFUSCATED;
      }

      String text = translateString(item.text);

      String[] split = cropStringBySize(text, modifiers, boxWidth, boxHeight - (atY - y), boxWidth - (atX - x), fr, item.scale);

      box1X = atX;
      box1Y = atY;
      box2X = x;
      box2W = x + boxWidth;

      for(int i = 0; i < split.length; i++) {
        if(i == split.length - 1) {
          box3X = atX;
          box3Y = atY;
        }

        String s = split[i];
        drawScaledString(fr, modifiers + s, atX, atY, 0, item.dropshadow, item.scale);

        if(i < split.length - 1) {
          atY += fr.FONT_HEIGHT;
          atX = x;
        }

        if(i == 0) {
          box2Y = atY;

          if(atX == x) {
            box1W = x + boxWidth;
          } else {
            box1W = atX;
          }
        }
      }

      box2H = atY;

      atX += fr.getStringWidth(split[split.length - 1]) * item.scale;
      if(atX - x >= boxWidth) {
        atX = x;
        atY += fr.FONT_HEIGHT * item.scale;
      }

      box3W = atX;
      box3H = (int) (atY + fr.FONT_HEIGHT * item.scale);

      if(item.tooltip != null && item.tooltip.length > 0) {
        // Uncomment to render bounding boxes for event handling
        /*drawGradientRect(box1X, box1Y, box1W, box1H, 0xFF00FF00, 0xFF00FF00);
        drawGradientRect(box2X, box2Y, box2W, box2H, 0xFFFF0000, 0xFFFF0000);
        drawGradientRect(box3X, box3Y, box3W, box3H, 0xFF0000FF, 0xFF0000FF);
        drawGradientRect(mouseX, mouseY, mouseX + 5, mouseY + 5, 0xFFFF00FF, 0xFFFFFF00);*/

        if((mouseX >= box1X && mouseX <= box1W && mouseY >= box1Y && mouseY <= box1H && box1X != box1W && box1Y != box1H) || (mouseX >= box2X && mouseX <= box2W && mouseY >= box2Y && mouseY <= box2H && box2X != box2W && box2Y != box2H) || (mouseX >= box3X && mouseX <= box3W && mouseY >= box3Y && mouseY <= box3H && box3X != box3W && box1Y != box3H)) {
          drawLabel = item.tooltip;
        }
      }

      if(item.action != null && !item.action.isEmpty()) {
        if((mouseX >= box1X && mouseX <= box1W && mouseY >= box1Y && mouseY <= box1H && box1X != box1W && box1Y != box1H) || (mouseX >= box2X && mouseX <= box2W && mouseY >= box2Y && mouseY <= box2H && box2X != box2W && box2Y != box2H) || (mouseX >= box3X && mouseX <= box3W && mouseY >= box3Y && mouseY <= box3H && box3X != box3W && box1Y != box3H)) {
          action = item.action;
        }
      }

      if(atY >= y + boxHeight) {
        fr.drawString("...", atX, atY, 0, item.dropshadow);
        break;
      }
      y = atY;
    }

    if(GuiBook.debug && action != null && !action.isEmpty()) {
      String[] label = drawLabel;
      drawLabel = new String[label != null ? label.length + 2 : 1];

      if(label != null) {
        for(int i = 0; i < label.length; i++) {
          drawLabel[i] = label[i];
        }
      }

      drawLabel[drawLabel.length > 1 ? drawLabel.length - 2 : 0] = "";
      drawLabel[drawLabel.length - 1] = TextFormatting.GRAY + "Action: " + action;
    }

    if(drawLabel != null) {
      drawHoveringText(drawLabel, mouseX, mouseY, fr);
    }

    return action;
  }

  public static String translateString(String s) {
    s = s.replace("$$(", "$\0(").replace(")$$", ")\0$");

    while(s.contains("$(") && s.contains(")$") && s.indexOf("$(") < s.indexOf(")$")) {
      String loc = s.substring(s.indexOf("$(") + 2, s.indexOf(")$"));
      s = s.replace("$(" + loc + ")$", I18n.translateToLocal(loc));
    }

    if(s.indexOf("$(") > s.indexOf(")$") || s.contains(")$")) {
      Mantle.logger
          .error("[Books] [TextDataRenderer] Detected unbalanced localization symbols \"$(\" and \")$\" in string: \"" + s + "\".");
    }

    return s.replace("$\0(", "$(").replace(")\0$", ")$");
  }

  public static String[] cropStringBySize(String s, String modifiers, int width, int height, FontRenderer fr, float scale) {
    return cropStringBySize(s, modifiers, width, height, width, fr, scale);
  }

  public static String[] cropStringBySize(String s, String modifiers, int width, int height, int firstWidth, FontRenderer fr, float scale) {
    int curWidth = 0;
    int curHeight = (int) (fr.FONT_HEIGHT * scale);

    for(int i = 0; i < s.length(); i++) {
      curWidth += fr.getStringWidth(modifiers + Character.toString(s.charAt(i))) * scale;

      if(s.charAt(i) == '\n' || (curHeight == (int) (fr.FONT_HEIGHT * scale) && curWidth > firstWidth) || (curHeight != (int) (fr.FONT_HEIGHT * scale) && curWidth > width)) {
        int oldI = i;
        while(i >= 0 && s.charAt(i) != ' ') {
          i--;
        }
        if(i <= 0) {
          i = oldI;
        }

        s = s.substring(0, i).trim() + "\r" + s.substring(i + (i == oldI ? 0 : 1)).trim();

        i++;
        curWidth = 0;
        curHeight += fr.FONT_HEIGHT * scale;

        if(curHeight >= height) {
          return s.substring(0, i).trim().split("\r");
        }
      }
    }

    return s.split("\r");
  }

  //BEGIN METHODS FROM GUI
  private static void drawHoveringText(String[] textLines, int x, int y, FontRenderer font) {
    GuiUtils.drawHoveringText(ImmutableList.copyOf(textLines), x, y, GuiBook.PAGE_WIDTH, GuiBook.PAGE_HEIGHT, GuiBook.PAGE_WIDTH, font);
    RenderHelper.disableStandardItemLighting();
  }

  public static void drawScaledString(FontRenderer font, String text, float x, float y, int color, boolean dropShadow, float scale) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x, y, 0);
    GlStateManager.scale(scale, scale, 1F);
    font.drawString(text, 0, 0, color, dropShadow);
    GlStateManager.popMatrix();
  }

  private static void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
    float f = (float) (startColor >> 24 & 255) / 255.0F;
    float f1 = (float) (startColor >> 16 & 255) / 255.0F;
    float f2 = (float) (startColor >> 8 & 255) / 255.0F;
    float f3 = (float) (startColor & 255) / 255.0F;
    float f4 = (float) (endColor >> 24 & 255) / 255.0F;
    float f5 = (float) (endColor >> 16 & 255) / 255.0F;
    float f6 = (float) (endColor >> 8 & 255) / 255.0F;
    float f7 = (float) (endColor & 255) / 255.0F;
    GlStateManager.disableTexture2D();
    GlStateManager.disableAlpha();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    GlStateManager.shadeModel(7425);
    Tessellator tessellator = Tessellator.getInstance();
    VertexBuffer vertexBuffer = tessellator.getBuffer();
    vertexBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
    vertexBuffer.pos((double) right, (double) top, 0D).color(f1, f2, f3, f).endVertex();
    vertexBuffer.pos((double) left, (double) top, 0D).color(f1, f2, f3, f).endVertex();
    vertexBuffer.pos((double) left, (double) bottom, 0D).color(f5, f6, f7, f4).endVertex();
    vertexBuffer.pos((double) right, (double) bottom, 0D).color(f5, f6, f7, f4).endVertex();
    tessellator.draw();
    GlStateManager.shadeModel(7424);
    GlStateManager.enableAlpha();
    GlStateManager.enableTexture2D();
  }
  //END METHODS FROM GUI
}
