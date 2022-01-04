package slimeknights.mantle.client.screen.book;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.gui.GuiUtils;
import org.apache.commons.lang3.StringUtils;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.data.element.TextData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TextDataRenderer {

  /**
   * @deprecated Call drawText with tooltip param and then call drawTooltip separately on the tooltip layer to prevent overlap
   */
  @Deprecated
  public static String drawText(PoseStack matrixStack, int x, int y, int boxWidth, int boxHeight, TextData[] data, int mouseX, int mouseY, Font fr, BookScreen parent) {
    List<Component> tooltip = new ArrayList<>();
    String action = drawText(matrixStack, x, y, boxWidth, boxHeight, data, mouseX, mouseY, fr, tooltip);

    if (tooltip.size() > 0) {
      parent.renderTooltip(matrixStack, tooltip, Optional.empty(), mouseX, mouseY, fr);
    }

    return action;
  }

  public static String drawText(PoseStack matrixStack, int x, int y, int boxWidth, int boxHeight, TextData[] data, int mouseX, int mouseY, Font fr, List<Component> tooltip) {
    String action = "";

    int atX = x;
    int atY = y;

    float prevScale = 1.F;

    for (TextData item : data) {
      int box1X, box1Y, box1W = 9999, box1H = y + fr.lineHeight;
      int box2X, box2Y = 9999, box2W, box2H;
      int box3X = 9999, box3Y = 9999, box3W, box3H;

      if (item == null || item.text == null || item.text.isEmpty()) {
        continue;
      }
      if (item.text.equals("\n")) {
        atX = x;
        atY += fr.lineHeight;
        continue;
      }

      if (item.paragraph) {
        atX = x;
        atY += fr.lineHeight * 2 * prevScale;
      }

      prevScale = item.scale;

      String modifiers = "";

      if (item.useOldColor) {
        ChatFormatting colFormat = ChatFormatting.getByName(item.color);
        if(colFormat != null) {
          modifiers += colFormat;
        } else {
          modifiers += "unknown color"; // more descriptive than null

          // This will spam the console, but that makes the error more obvious
          Mantle.logger.error("Failed to parse color: " + item.color + " for text rendering.");
        }
      }

      if (item.bold) {
        modifiers += ChatFormatting.BOLD;
      }
      if (item.italic) {
        modifiers += ChatFormatting.ITALIC;
      }
      if (item.underlined) {
        modifiers += ChatFormatting.UNDERLINE;
      }
      if (item.strikethrough) {
        modifiers += ChatFormatting.STRIKETHROUGH;
      }
      if (item.obfuscated) {
        modifiers += ChatFormatting.OBFUSCATED;
      }

      String text = translateString(item.text);

      String[] split = cropStringBySize(text, modifiers, boxWidth, boxHeight - (atY - y), boxWidth - (atX - x), fr, item.scale);

      box1X = atX;
      box1Y = atY;
      box2X = x;
      box2W = x + boxWidth;

      for (int i = 0; i < split.length; i++) {
        if (i == split.length - 1) {
          box3X = atX;
          box3Y = atY;
        }

        String s = split[i];
        drawScaledString(matrixStack, fr, modifiers + s, atX, atY, item.rgbColor, item.dropshadow, item.scale);

        if (i < split.length - 1) {
          atY += fr.lineHeight;
          atX = x;
        }

        if (i == 0) {
          box2Y = atY;

          if (atX == x) {
            box1W = x + boxWidth;
          } else {
            box1W = atX;
          }
        }
      }

      box2H = atY;

      atX += fr.width(split[split.length - 1]) * item.scale;
      if (atX - x >= boxWidth) {
        atX = x;
        atY += fr.lineHeight * item.scale;
      }

      box3W = atX;
      box3H = (int) (atY + fr.lineHeight * item.scale);

      boolean mouseInside = (mouseX >= box1X && mouseX <= box1W && mouseY >= box1Y && mouseY <= box1H && box1X != box1W && box1Y != box1H)
                            || (mouseX >= box2X && mouseX <= box2W && mouseY >= box2Y && mouseY <= box2H && box2X != box2W && box2Y != box2H)
                            || (mouseX >= box3X && mouseX <= box3W && mouseY >= box3Y && mouseY <= box3H && box3X != box3W && box1Y != box3H);
      if (item.tooltip != null && item.tooltip.length > 0) {
        if (BookScreen.debug) {
          Matrix4f matrix = matrixStack.last().pose();
          GuiUtils.drawGradientRect(matrix, 0, box1X,  box1Y,  box1W,      box1H,      0xFF00FF00, 0xFF00FF00);
          GuiUtils.drawGradientRect(matrix, 0, box2X,  box2Y,  box2W,      box2H,      0xFFFF0000, 0xFFFF0000);
          GuiUtils.drawGradientRect(matrix, 0, box3X,  box3Y,  box3W,      box3H,      0xFF0000FF, 0xFF0000FF);
          GuiUtils.drawGradientRect(matrix, 0, mouseX, mouseY, mouseX + 5, mouseY + 5, 0xFFFF00FF, 0xFFFFFF00);
        }

        if (mouseInside) {
          tooltip.addAll(Arrays.asList(item.tooltip));
        }
      }

      if (item.action != null && !item.action.isEmpty()) {
        if (mouseInside) {
          action = item.action;
        }
      }

      if (atY >= y + boxHeight) {
        if (item.dropshadow) {
          fr.drawShadow(matrixStack, "...", atX, atY, 0);
        } else {
          fr.draw(matrixStack, "...", atX, atY, 0);
        }
        break;
      }
      y = atY;
    }

    if (BookScreen.debug && !action.isEmpty()) {
      tooltip.add(TextComponent.EMPTY);
      tooltip.add(new TextComponent("Action: " + action).withStyle(ChatFormatting.GRAY));
    }

    return action;
  }

  public static String translateString(String s) {
    s = s.replace("$$(", "$\0(").replace(")$$", ")\0$");

    while (s.contains("$(") && s.contains(")$") && s.indexOf("$(") < s.indexOf(")$")) {
      String loc = s.substring(s.indexOf("$(") + 2, s.indexOf(")$"));
      s = s.replace("$(" + loc + ")$", I18n.get(loc));
    }

    if (s.indexOf("$(") > s.indexOf(")$") || s.contains(")$")) {
      Mantle.logger.error("[Books] [TextDataRenderer] Detected unbalanced localization symbols \"$(\" and \")$\" in string: \"" + s + "\".");
    }

    return s.replace("$\0(", "$(").replace(")\0$", ")$");
  }

  public static String[] cropStringBySize(String s, String modifiers, int width, int height, Font fr, float scale) {
    return cropStringBySize(s, modifiers, width, height, width, fr, scale);
  }

  public static String[] cropStringBySize(String s, String modifiers, int width, int height, int firstWidth, Font fr, float scale) {
    int curWidth = 0;
    int curHeight = (int) (fr.lineHeight * scale);

    for (int i = 0; i < s.length(); i++) {
      curWidth += fr.width(modifiers + s.charAt(i)) * scale;

      if (s.charAt(i) == '\n' || (curHeight == (int) (fr.lineHeight * scale) && curWidth > firstWidth) || (curHeight != (int) (fr.lineHeight * scale) && curWidth > width)) {
        int oldI = i;
        if (s.charAt(i) != '\n') {
          while (i >= 0 && s.charAt(i) != ' ') {
            i--;
          }
          if (i <= 0) {
            i = oldI;
          }
        } else {
          oldI++;
        }

        s = s.substring(0, i) + "\r" + StringUtils.stripStart(s.substring(i + (i == oldI ? 0 : 1)), " ");

        i++;
        curWidth = 0;
        curHeight += fr.lineHeight * scale;

        if (curHeight >= height) {
          return s.substring(0, i).split("\r");
        }
      }
    }

    return s.split("\r");
  }

  //BEGIN METHODS FROM GUI
  public static void drawScaledString(PoseStack matrixStack, Font font, String text, float x, float y, int color, boolean dropShadow, float scale) {
    matrixStack.pushPose();
    matrixStack.translate(x, y, 0);
    matrixStack.scale(scale, scale, 1F);

    if (dropShadow) {
      font.drawShadow(matrixStack, text, 0, 0, color);
    } else {
      font.draw(matrixStack, text, 0, 0, color);
    }

    matrixStack.popPose();
  }
  //END METHODS FROM GUI
}
