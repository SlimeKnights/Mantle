package slimeknights.mantle.client.screen.book;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.render.RenderingHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class TextDataRenderer {

  /**
   * @deprecated Call drawText with tooltip param and then call drawTooltip separately on the tooltip layer to prevent overlap
   */
  @Nullable
  public static String drawText(MatrixStack matrixStack, int x, int y, int boxWidth, int boxHeight, TextData[] data, int mouseX, int mouseY, FontRenderer fr) {
    List<ITextComponent> tooltip = new ArrayList<ITextComponent>();
    String action = drawText(matrixStack, x, y, boxWidth, boxHeight, data, mouseX, mouseY, fr, tooltip);

    if (tooltip.size() > 0) {
      drawTooltip(matrixStack, tooltip, mouseX, mouseY, fr);
    }

    return action;
  }

  @Nullable
  public static String drawText(MatrixStack matrixStack, int x, int y, int boxWidth, int boxHeight, TextData[] data, int mouseX, int mouseY, FontRenderer fr, List<ITextComponent> tooltip) {
    String action = "";

    int atX = x;
    int atY = y;

    float prevScale = 1.F;

    for (TextData item : data) {
      int box1X, box1Y, box1W = 9999, box1H = y + fr.FONT_HEIGHT;
      int box2X, box2Y = 9999, box2W, box2H;
      int box3X = 9999, box3Y = 9999, box3W, box3H;

      if (item == null || item.text == null || item.text.isEmpty()) {
        continue;
      }
      if (item.text.equals("\n")) {
        atX = x;
        atY += fr.FONT_HEIGHT;
        continue;
      }

      if (item.paragraph) {
        atX = x;
        atY += fr.FONT_HEIGHT * 2 * prevScale;
      }

      prevScale = item.scale;

      String modifiers = "";

      modifiers += TextFormatting.getValueByName(item.color);

      if (item.bold) {
        modifiers += TextFormatting.BOLD;
      }
      if (item.italic) {
        modifiers += TextFormatting.ITALIC;
      }
      if (item.underlined) {
        modifiers += TextFormatting.UNDERLINE;
      }
      if (item.strikethrough) {
        modifiers += TextFormatting.STRIKETHROUGH;
      }
      if (item.obfuscated) {
        modifiers += TextFormatting.OBFUSCATED;
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
        drawScaledString(matrixStack, fr, modifiers + s, atX, atY, 0, item.dropshadow, item.scale);

        if (i < split.length - 1) {
          atY += fr.FONT_HEIGHT;
          atX = x;
        }

        if (i == 0) {
          box2Y = atY;

          if (atX == x) {
            box1W = x + boxWidth;
          }
          else {
            box1W = atX;
          }
        }
      }

      box2H = atY;

      atX += fr.getStringWidth(split[split.length - 1]) * item.scale;
      if (atX - x >= boxWidth) {
        atX = x;
        atY += fr.FONT_HEIGHT * item.scale;
      }

      box3W = atX;
      box3H = (int) (atY + fr.FONT_HEIGHT * item.scale);

      if (item.tooltip != null && item.tooltip.length > 0) {
        // Uncomment to render bounding boxes for event handling
        /*drawGradientRect(box1X, box1Y, box1W, box1H, 0xFF00FF00, 0xFF00FF00);
        drawGradientRect(box2X, box2Y, box2W, box2H, 0xFFFF0000, 0xFFFF0000);
        drawGradientRect(box3X, box3Y, box3W, box3H, 0xFF0000FF, 0xFF0000FF);
        drawGradientRect(mouseX, mouseY, mouseX + 5, mouseY + 5, 0xFFFF00FF, 0xFFFFFF00);*/

        if ((mouseX >= box1X && mouseX <= box1W && mouseY >= box1Y && mouseY <= box1H && box1X != box1W && box1Y != box1H) || (mouseX >= box2X && mouseX <= box2W && mouseY >= box2Y && mouseY <= box2H && box2X != box2W && box2Y != box2H) || (mouseX >= box3X && mouseX <= box3W && mouseY >= box3Y && mouseY <= box3H && box3X != box3W && box1Y != box3H)) {
          tooltip.addAll(Arrays.asList(item.tooltip));
        }
      }

      if (item.action != null && !item.action.isEmpty()) {
        if ((mouseX >= box1X && mouseX <= box1W && mouseY >= box1Y && mouseY <= box1H && box1X != box1W && box1Y != box1H) || (mouseX >= box2X && mouseX <= box2W && mouseY >= box2Y && mouseY <= box2H && box2X != box2W && box2Y != box2H) || (mouseX >= box3X && mouseX <= box3W && mouseY >= box3Y && mouseY <= box3H && box3X != box3W && box1Y != box3H)) {
          action = item.action;
        }
      }

      if (atY >= y + boxHeight) {
        if (item.dropshadow) {
          fr.drawStringWithShadow(matrixStack, "...", atX, atY, 0);
        }
        else {
          fr.drawString(matrixStack, "...", atX, atY, 0);
        }
        break;
      }
      y = atY;
    }

    if (BookScreen.debug && action != null && !action.isEmpty()) {
      tooltip.add(StringTextComponent.EMPTY);
      tooltip.add(new StringTextComponent("Action: " + action).mergeStyle(TextFormatting.GRAY));
    }

    return action;
  }

  public static String translateString(String s) {
    s = s.replace("$$(", "$\0(").replace(")$$", ")\0$");

    while (s.contains("$(") && s.contains(")$") && s.indexOf("$(") < s.indexOf(")$")) {
      String loc = s.substring(s.indexOf("$(") + 2, s.indexOf(")$"));
      s = s.replace("$(" + loc + ")$", I18n.format(loc));
    }

    if (s.indexOf("$(") > s.indexOf(")$") || s.contains(")$")) {
      Mantle.logger.error("[Books] [TextDataRenderer] Detected unbalanced localization symbols \"$(\" and \")$\" in string: \"" + s + "\".");
    }

    return s.replace("$\0(", "$(").replace(")\0$", ")$");
  }

  public static String[] cropStringBySize(String s, String modifiers, int width, int height, FontRenderer fr, float scale) {
    return cropStringBySize(s, modifiers, width, height, width, fr, scale);
  }

  public static String[] cropStringBySize(String s, String modifiers, int width, int height, int firstWidth, FontRenderer fr, float scale) {
    int curWidth = 0;
    int curHeight = (int) (fr.FONT_HEIGHT * scale);

    for (int i = 0; i < s.length(); i++) {
      curWidth += fr.getStringWidth(modifiers + s.charAt(i)) * scale;

      if (s.charAt(i) == '\n' || (curHeight == (int) (fr.FONT_HEIGHT * scale) && curWidth > firstWidth) || (curHeight != (int) (fr.FONT_HEIGHT * scale) && curWidth > width)) {
        int oldI = i;
        if(s.charAt(i) != '\n') {
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
        curHeight += fr.FONT_HEIGHT * scale;

        if (curHeight >= height) {
          return s.substring(0, i).split("\r");
        }
      }
    }

    return s.split("\r");
  }

  //BEGIN METHODS FROM GUI
  public static void drawTooltip(MatrixStack matrixStack, List<ITextComponent> textLines, int mouseX, int mouseY, FontRenderer font) {
    RenderingHelper.drawHoveringText(matrixStack, textLines, mouseX, mouseY, BookScreen.PAGE_WIDTH, BookScreen.PAGE_HEIGHT, BookScreen.PAGE_WIDTH, font);
    RenderHelper.disableStandardItemLighting();
  }

  public static void drawScaledString(MatrixStack matrixStack, FontRenderer font, String text, float x, float y, int color, boolean dropShadow, float scale) {
    RenderSystem.pushMatrix();
    RenderSystem.translatef(x, y, 0);
    RenderSystem.scalef(scale, scale, 1F);
    if (dropShadow) {
      font.drawStringWithShadow(matrixStack, text, 0, 0, color);
    }
    else {
      font.drawString(matrixStack, text, 0, 0, color);
    }
    RenderSystem.popMatrix();
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
    RenderSystem.disableTexture();
    RenderSystem.disableAlphaTest();
    RenderSystem.blendFuncSeparate(770, 771, 1, 0);
    RenderSystem.shadeModel(7425);
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder vertexBuffer = tessellator.getBuffer();
    vertexBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
    vertexBuffer.pos((double) right, (double) top, 0D).color(f1, f2, f3, f).endVertex();
    vertexBuffer.pos((double) left, (double) top, 0D).color(f1, f2, f3, f).endVertex();
    vertexBuffer.pos((double) left, (double) bottom, 0D).color(f5, f6, f7, f4).endVertex();
    vertexBuffer.pos((double) right, (double) bottom, 0D).color(f5, f6, f7, f4).endVertex();
    tessellator.draw();
    RenderSystem.shadeModel(7424);
    RenderSystem.enableAlphaTest();
    RenderSystem.enableTexture();
  }
  //END METHODS FROM GUI
}
